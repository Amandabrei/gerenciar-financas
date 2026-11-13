package com.gerenciar.financas.transactions.application.service;

import com.gerenciar.financas.transactions.application.port.in.CreateTransactionUseCase;
import com.gerenciar.financas.transactions.application.port.out.TransactionRepositoryPort;
import com.gerenciar.financas.transactions.application.port.out.UserValidationPort;
import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialInfo;
import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialPort;
import com.gerenciar.financas.transactions.application.port.out.exchange.ExchangeRateInfo;
import com.gerenciar.financas.transactions.application.port.out.exchange.ExchangeRatePort;
import com.gerenciar.financas.transactions.core.enums.TransactionStatus;
import com.gerenciar.financas.transactions.core.enums.TransactionType;
import com.gerenciar.financas.transactions.core.exception.InvalidTransactionException;
import com.gerenciar.financas.transactions.core.exception.UserNotFoundException;
import com.gerenciar.financas.transactions.core.model.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class CreateTransactionService implements CreateTransactionUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(CreateTransactionService.class);

    private static final String ACCOUNT_UNAVAILABLE =
            "Dados financeiros indisponíveis. Operação não aprovada.";

    private final TransactionRepositoryPort transactionRepositoryPort;
    private final UserValidationPort userValidationPort;
    private final AccountFinancialPort accountFinancialPort;
    private final ExchangeRatePort exchangeRatePort;
    private final Clock clock;

    public CreateTransactionService(
            TransactionRepositoryPort transactionRepositoryPort,
            UserValidationPort userValidationPort,
            AccountFinancialPort accountFinancialPort,
            ExchangeRatePort exchangeRatePort,
            Clock clock) {

        this.transactionRepositoryPort = transactionRepositoryPort;
        this.userValidationPort = userValidationPort;
        this.accountFinancialPort = accountFinancialPort;
        this.exchangeRatePort = exchangeRatePort;
        this.clock = clock;
    }

    private BigDecimal calculateApprovedDailySpent(UUID ownerId) {
        LocalDate today = LocalDate.now(clock);

        return transactionRepositoryPort.findAllByOwnerId(ownerId)
                .stream()
                .filter(transaction ->
                        transaction.getStatus() == TransactionStatus.APPROVED)
                .filter(transaction ->
                        transaction.getCreatedAt().toLocalDate().equals(today))
                .filter(transaction ->
                        transaction.getType() == TransactionType.WITHDRAWAL
                                || transaction.getType() == TransactionType.TRANSFER
                                || transaction.getType() == TransactionType.PURCHASE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Transaction createDeposit(
            UUID ownerId,
            BigDecimal amount,
            String description) {

        validateUser(ownerId);
        validateAmount(amount);

        AccountFinancialInfo account = getAccountSafely(ownerId);

        if (account == null || account.balance() == null) {
            return saveRejectedTransaction(
                    ownerId, null, TransactionType.DEPOSIT,
                    amount, description, "DEPOSIT",
                    ACCOUNT_UNAVAILABLE
            );
        }

        BigDecimal previousBalance = account.balance();
        BigDecimal newBalance = previousBalance.add(amount);

        Transaction transaction = createTransaction(
                ownerId, null, TransactionType.DEPOSIT,
                amount, description, "DEPOSIT",
                TransactionStatus.APPROVED, null,
                previousBalance, newBalance
        );

        return updateSingleBalanceAndSave(
                ownerId,
                previousBalance,
                newBalance,
                transaction,
                "Não foi possível atualizar o saldo do depósito. Operação não aprovada."
        );
    }

    @Override
    public Transaction createWithdrawal(
            UUID ownerId,
            BigDecimal amount,
            String description) {

        validateUser(ownerId);

        ValidationResult validation =
                validateOutgoingTransaction(ownerId, amount);

        if (validation.rejectionReason() != null) {
            BigDecimal knownBalance = validation.account() != null
                    ? validation.account().balance()
                    : null;

            return saveRejectedTransaction(
                    ownerId, null, TransactionType.WITHDRAWAL,
                    amount, description, "WITHDRAWAL",
                    validation.rejectionReason(),
                    knownBalance, knownBalance
            );
        }

        BigDecimal previousBalance = validation.account().balance();
        BigDecimal newBalance = previousBalance.subtract(amount);

        Transaction transaction = createTransaction(
                ownerId, null, TransactionType.WITHDRAWAL,
                amount, description, "WITHDRAWAL",
                TransactionStatus.APPROVED, null,
                previousBalance, newBalance
        );

        return updateSingleBalanceAndSave(
                ownerId,
                previousBalance,
                newBalance,
                transaction,
                "Não foi possível atualizar o saldo do saque. Operação não aprovada."
        );
    }

    @Override
    public Transaction createTransfer(
            UUID ownerId,
            UUID destinationUserId,
            BigDecimal amount,
            String description) {

        validateUser(ownerId);
        validateUser(destinationUserId);

        if (ownerId.equals(destinationUserId)) {
            throw new InvalidTransactionException(
                    "Não é possível transferir para o próprio usuário."
            );
        }

        ValidationResult validation =
                validateOutgoingTransaction(ownerId, amount);

        if (validation.rejectionReason() != null) {
            BigDecimal knownBalance = validation.account() != null
                    ? validation.account().balance()
                    : null;

            return saveRejectedTransaction(
                    ownerId, destinationUserId, TransactionType.TRANSFER,
                    amount, description, "TRANSFER",
                    validation.rejectionReason(),
                    knownBalance, knownBalance
            );
        }

        BigDecimal sourcePreviousBalance = validation.account().balance();

        AccountFinancialInfo destinationAccount =
                getAccountSafely(destinationUserId);

        if (destinationAccount == null || destinationAccount.balance() == null) {
            return saveRejectedTransaction(
                    ownerId, destinationUserId, TransactionType.TRANSFER,
                    amount, description, "TRANSFER",
                    "Conta financeira do destinatário não encontrada. Transferência não aprovada.",
                    sourcePreviousBalance, sourcePreviousBalance
            );
        }

        BigDecimal sourceNewBalance = sourcePreviousBalance.subtract(amount);

        BigDecimal destinationPreviousBalance = destinationAccount.balance();
        BigDecimal destinationNewBalance = destinationPreviousBalance.add(amount);

        Transaction transaction = createTransaction(
                ownerId, destinationUserId, TransactionType.TRANSFER,
                amount, description, "TRANSFER",
                TransactionStatus.APPROVED, null,
                sourcePreviousBalance, sourceNewBalance
        );

        return updateTransferBalancesAndSave(
                ownerId,
                sourcePreviousBalance,
                sourceNewBalance,
                destinationUserId,
                destinationPreviousBalance,
                destinationNewBalance,
                transaction
        );
    }

    @Override
    public Transaction createPurchase(
            UUID ownerId,
            BigDecimal amount,
            String description,
            String category) {

        validateUser(ownerId);

        if (category == null || category.isBlank()) {
            throw new InvalidTransactionException(
                    "A categoria da compra é obrigatória."
            );
        }

        ValidationResult validation =
                validateOutgoingTransaction(ownerId, amount);

        if (validation.rejectionReason() != null) {
            BigDecimal knownBalance = validation.account() != null
                    ? validation.account().balance()
                    : null;

            return saveRejectedTransaction(
                    ownerId, null, TransactionType.PURCHASE,
                    amount, description, category,
                    validation.rejectionReason(),
                    knownBalance, knownBalance
            );
        }

        BigDecimal previousBalance = validation.account().balance();
        BigDecimal newBalance = previousBalance.subtract(amount);

        Transaction transaction = createTransaction(
                ownerId, null, TransactionType.PURCHASE,
                amount, description, category,
                TransactionStatus.APPROVED, null,
                previousBalance, newBalance
        );

        return updateSingleBalanceAndSave(
                ownerId,
                previousBalance,
                newBalance,
                transaction,
                "Não foi possível atualizar o saldo da compra. Operação não aprovada."
        );
    }

    private Transaction updateSingleBalanceAndSave(
            UUID userId,
            BigDecimal previousBalance,
            BigDecimal newBalance,
            Transaction transaction,
            String rejectionReason) {

        try {
            accountFinancialPort.updateBalance(userId, newBalance);

        } catch (RuntimeException exception) {
            log.error(
                    "Falha ao atualizar saldo do usuário {}.",
                    userId,
                    exception
            );

            boolean compensated =
                    compensateBalance(userId, previousBalance);

            BigDecimal knownBalance = compensated ? previousBalance : null;

            return saveRejectedTransaction(
                    transaction.getOwnerId(),
                    transaction.getDestinationUserId(),
                    transaction.getType(),
                    transaction.getAmount(),
                    transaction.getDescription(),
                    transaction.getCategory(),
                    rejectionReason,
                    knownBalance,
                    knownBalance
            );
        }

        try {
            return transactionRepositoryPort.save(transaction);

        } catch (RuntimeException exception) {
            log.error(
                    "Falha ao salvar transação. Tentando restaurar saldo do usuário {}.",
                    userId,
                    exception
            );

            compensateBalance(userId, previousBalance);
            throw exception;
        }
    }

    private Transaction updateTransferBalancesAndSave(
            UUID sourceUserId,
            BigDecimal sourcePreviousBalance,
            BigDecimal sourceNewBalance,
            UUID destinationUserId,
            BigDecimal destinationPreviousBalance,
            BigDecimal destinationNewBalance,
            Transaction transaction) {

        boolean sourceUpdateAttempted = false;
        boolean destinationUpdateAttempted = false;

        try {
            sourceUpdateAttempted = true;

            accountFinancialPort.updateBalance(
                    sourceUserId,
                    sourceNewBalance
            );

            destinationUpdateAttempted = true;

            accountFinancialPort.updateBalance(
                    destinationUserId,
                    destinationNewBalance
            );

        } catch (RuntimeException exception) {
            log.error(
                    "Falha ao atualizar saldos da transferência entre {} e {}.",
                    sourceUserId,
                    destinationUserId,
                    exception
            );

            boolean destinationCompensated = !destinationUpdateAttempted
                    || compensateBalance(
                    destinationUserId,
                    destinationPreviousBalance
            );

            boolean sourceCompensated = !sourceUpdateAttempted
                    || compensateBalance(
                    sourceUserId,
                    sourcePreviousBalance
            );

            boolean fullyCompensated =
                    sourceCompensated && destinationCompensated;

            BigDecimal knownBalance =
                    fullyCompensated ? sourcePreviousBalance : null;

            return saveRejectedTransaction(
                    transaction.getOwnerId(),
                    transaction.getDestinationUserId(),
                    transaction.getType(),
                    transaction.getAmount(),
                    transaction.getDescription(),
                    transaction.getCategory(),
                    "Não foi possível atualizar os saldos da transferência. Operação não aprovada.",
                    knownBalance,
                    knownBalance
            );
        }

        try {
            return transactionRepositoryPort.save(transaction);

        } catch (RuntimeException exception) {
            log.error(
                    "Falha ao salvar transferência. Tentando restaurar os saldos.",
                    exception
            );

            compensateBalance(
                    destinationUserId,
                    destinationPreviousBalance
            );

            compensateBalance(
                    sourceUserId,
                    sourcePreviousBalance
            );

            throw exception;
        }
    }

    private boolean compensateBalance(
            UUID userId,
            BigDecimal previousBalance) {

        try {
            accountFinancialPort.updateBalance(userId, previousBalance);
            return true;

        } catch (RuntimeException exception) {
            log.error(
                    "Não foi possível compensar a atualização do saldo do usuário {}. "
                            + "É necessária reconciliação financeira.",
                    userId,
                    exception
            );

            return false;
        }
    }

    private Transaction saveRejectedTransaction(
            UUID ownerId,
            UUID destinationUserId,
            TransactionType type,
            BigDecimal amount,
            String description,
            String category,
            String rejectionReason) {

        return saveRejectedTransaction(
                ownerId,
                destinationUserId,
                type,
                amount,
                description,
                category,
                rejectionReason,
                null,
                null
        );
    }

    private Transaction saveRejectedTransaction(
            UUID ownerId,
            UUID destinationUserId,
            TransactionType type,
            BigDecimal amount,
            String description,
            String category,
            String rejectionReason,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter) {

        Transaction transaction = createTransaction(
                ownerId,
                destinationUserId,
                type,
                amount,
                description,
                category,
                TransactionStatus.REJECTED,
                rejectionReason,
                balanceBefore,
                balanceAfter
        );

        return transactionRepositoryPort.save(transaction);
    }

    private ValidationResult validateOutgoingTransaction(
            UUID ownerId,
            BigDecimal amount) {

        validateAmount(amount);

        AccountFinancialInfo financialInfo = getAccountSafely(ownerId);

        if (financialInfo == null
                || financialInfo.balance() == null
                || financialInfo.dailyLimit() == null
                || financialInfo.transactionLimit() == null) {

            return new ValidationResult(null, ACCOUNT_UNAVAILABLE);
        }

        if (amount.compareTo(financialInfo.transactionLimit()) > 0) {
            return new ValidationResult(
                    financialInfo,
                    "O valor ultrapassa o limite por transação."
            );
        }

        BigDecimal approvedDailySpent =
                calculateApprovedDailySpent(ownerId);

        BigDecimal totalDailySpent =
                approvedDailySpent.add(amount);

        if (totalDailySpent.compareTo(financialInfo.dailyLimit()) > 0) {
            return new ValidationResult(
                    financialInfo,
                    "O limite diário foi ultrapassado."
            );
        }

        if (amount.compareTo(financialInfo.balance()) > 0) {
            return new ValidationResult(
                    financialInfo,
                    "Saldo insuficiente."
            );
        }

        return new ValidationResult(financialInfo, null);
    }

    private AccountFinancialInfo getAccountSafely(UUID userId) {
        try {
            return accountFinancialPort.getAccountFinancialInfo(userId);

        } catch (RuntimeException exception) {
            log.error(
                    "Não foi possível consultar a conta financeira do usuário {}.",
                    userId,
                    exception
            );

            return null;
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException(
                    "O valor deve ser maior que zero."
            );
        }
    }

    private void validateUser(UUID userId) {
        if (userId == null || !userValidationPort.userExists(userId)) {
            throw new UserNotFoundException("Usuário não encontrado.");
        }
    }

    private Transaction createTransaction(
            UUID ownerId,
            UUID destinationUserId,
            TransactionType type,
            BigDecimal amount,
            String description,
            String category,
            TransactionStatus status,
            String rejectionReason) {

        return createTransaction(
                ownerId,
                destinationUserId,
                type,
                amount,
                description,
                category,
                status,
                rejectionReason,
                null,
                null
        );
    }

    private Transaction createTransaction(
            UUID ownerId,
            UUID destinationUserId,
            TransactionType type,
            BigDecimal amount,
            String description,
            String category,
            TransactionStatus status,
            String rejectionReason,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter) {

        Optional<ExchangeRateInfo> usdRate =
                getExchangeRateSafely("USD");

        Optional<ExchangeRateInfo> eurRate =
                getExchangeRateSafely("EUR");

        return new Transaction(
                UUID.randomUUID(),
                ownerId,
                destinationUserId,
                type,
                amount,
                LocalDateTime.now(clock),
                description,
                category,
                status,
                rejectionReason,
                null,
                usdRate.map(ExchangeRateInfo::rate).orElse(null),
                usdRate.map(ExchangeRateInfo::quoteDate).orElse(null),
                eurRate.map(ExchangeRateInfo::rate).orElse(null),
                eurRate.map(ExchangeRateInfo::quoteDate).orElse(null),
                balanceBefore,
                balanceAfter
        );
    }

    private Optional<ExchangeRateInfo> getExchangeRateSafely(String currency) {
        try {
            Optional<ExchangeRateInfo> result =
                    exchangeRatePort.getExchangeRate(currency);

            return result != null ? result : Optional.empty();

        } catch (RuntimeException exception) {
            log.warn(
                    "Não foi possível consultar a cotação de {}. "
                            + "A transação será registrada sem essa cotação.",
                    currency,
                    exception
            );

            return Optional.empty();
        }
    }

    private record ValidationResult(
            AccountFinancialInfo account,
            String rejectionReason) {
    }
}
