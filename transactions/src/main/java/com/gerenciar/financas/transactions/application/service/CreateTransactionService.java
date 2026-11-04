package com.gerenciar.financas.transactions.application.service;

import com.gerenciar.financas.transactions.application.port.in.CreateTransactionUseCase;
import com.gerenciar.financas.transactions.application.port.out.TransactionRepositoryPort;
import com.gerenciar.financas.transactions.application.port.out.UserValidationPort;
import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialInfo;
import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialPort;
import com.gerenciar.financas.transactions.application.port.out.exchange.ExchangeRatePort;
import com.gerenciar.financas.transactions.domain.enums.TransactionStatus;
import com.gerenciar.financas.transactions.domain.enums.TransactionType;
import com.gerenciar.financas.transactions.domain.exception.InvalidTransactionException;
import com.gerenciar.financas.transactions.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class CreateTransactionService implements CreateTransactionUseCase {

    private final TransactionRepositoryPort transactionRepositoryPort;
    private final UserValidationPort userValidationPort;
    private final AccountFinancialPort accountFinancialPort;
    private final ExchangeRatePort exchangeRatePort;

    public CreateTransactionService(
            TransactionRepositoryPort transactionRepositoryPort,
            UserValidationPort userValidationPort,
            AccountFinancialPort accountFinancialPort,
            ExchangeRatePort exchangeRatePort
    ) {
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.userValidationPort = userValidationPort;
        this.accountFinancialPort = accountFinancialPort;
        this.exchangeRatePort = exchangeRatePort;
    }

    @Override
    public Transaction createDeposit(
            UUID ownerId,
            BigDecimal amount,
            String description
    ) {
        validateUser(ownerId);

        Transaction transaction = createTransaction(
                ownerId,
                null,
                TransactionType.DEPOSIT,
                amount,
                description,
                "DEPOSIT",
                TransactionStatus.APPROVED,
                null
        );

        return transactionRepositoryPort.save(transaction);
    }

    @Override
    public Transaction createWithdrawal(
            UUID ownerId,
            BigDecimal amount,
            String description
    ) {
        validateUser(ownerId);

        String rejectionReason = validateOutgoingTransaction(ownerId, amount);

        Transaction transaction = createTransaction(
                ownerId,
                null,
                TransactionType.WITHDRAWAL,
                amount,
                description,
                "WITHDRAWAL",
                rejectionReason == null
                        ? TransactionStatus.APPROVED
                        : TransactionStatus.REJECTED,
                rejectionReason
        );

        return transactionRepositoryPort.save(transaction);
    }

    @Override
    public Transaction createTransfer(
            UUID ownerId,
            UUID destinationUserId,
            BigDecimal amount,
            String description
    ) {
        validateUser(ownerId);
        validateUser(destinationUserId);

        if (ownerId.equals(destinationUserId)) {
            throw new InvalidTransactionException(
                    "Não é possível transferir para o próprio usuário."
            );
        }

        String rejectionReason = validateOutgoingTransaction(ownerId, amount);

        Transaction transaction = createTransaction(
                ownerId,
                destinationUserId,
                TransactionType.TRANSFER,
                amount,
                description,
                "TRANSFER",
                rejectionReason == null
                        ? TransactionStatus.APPROVED
                        : TransactionStatus.REJECTED,
                rejectionReason
        );

        return transactionRepositoryPort.save(transaction);
    }

    @Override
    public Transaction createPurchase(
            UUID ownerId,
            BigDecimal amount,
            String description,
            String category
    ) {
        validateUser(ownerId);

        if (category == null || category.isBlank()) {
            throw new InvalidTransactionException(
                    "A categoria da compra é obrigatória."
            );
        }

        String rejectionReason = validateOutgoingTransaction(ownerId, amount);

        Transaction transaction = createTransaction(
                ownerId,
                null,
                TransactionType.PURCHASE,
                amount,
                description,
                category,
                rejectionReason == null
                        ? TransactionStatus.APPROVED
                        : TransactionStatus.REJECTED,
                rejectionReason
        );

        return transactionRepositoryPort.save(transaction);
    }

    private void validateUser(UUID userId) {
        if (userId == null || !userValidationPort.userExists(userId)) {
            throw new InvalidTransactionException(
                    "Usuário não encontrado."
            );
        }
    }

    private String validateOutgoingTransaction(
            UUID ownerId,
            BigDecimal amount
    ) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException(
                    "O valor deve ser maior que zero."
            );
        }

        AccountFinancialInfo financialInfo;

        try {
            financialInfo = accountFinancialPort.getAccountFinancialInfo(ownerId);
        } catch (RuntimeException exception) {
            return "Não foi possível consultar saldo e limites. Operação não aprovada.";
        }

        if (financialInfo == null
                || financialInfo.balance() == null
                || financialInfo.dailyLimit() == null
                || financialInfo.transactionLimit() == null
                || financialInfo.dailySpent() == null) {
            return "Dados financeiros indisponíveis. Operação não aprovada.";
        }

        if (amount.compareTo(financialInfo.transactionLimit()) > 0) {
            return "O valor ultrapassa o limite por transação.";
        }

        if (financialInfo.dailySpent().add(amount)
                .compareTo(financialInfo.dailyLimit()) > 0) {
            return "O limite diário foi ultrapassado.";
        }

        if (amount.compareTo(financialInfo.balance()) > 0) {
            return "Saldo insuficiente.";
        }

        return null;
    }

    private Transaction createTransaction(
            UUID ownerId,
            UUID destinationUserId,
            TransactionType type,
            BigDecimal amount,
            String description,
            String category,
            TransactionStatus status,
            String rejectionReason
    ) {
        return new Transaction(
                UUID.randomUUID(),
                ownerId,
                destinationUserId,
                type,
                amount,
                LocalDateTime.now(),
                description,
                category,
                status,
                rejectionReason,
                null
        );
    }
}