package com.gerenciar.financas.transactions.core.service;

import com.gerenciar.financas.transactions.core.enums.TransactionStatus;
import com.gerenciar.financas.transactions.core.enums.TransactionType;
import com.gerenciar.financas.transactions.core.exception.InvalidTransactionException;
import com.gerenciar.financas.transactions.core.model.Transaction;

import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionCompensationService { //nao esta sendo usada mas vou deixar

    public Transaction createCompensation(
            Transaction original,
            TransactionStatus status,
            String rejectionReason
    ) {
        if (original == null) {
            throw new InvalidTransactionException(
                    "A transação original é obrigatória."
            );
        }

        if (original.getStatus() != TransactionStatus.APPROVED) {
            throw new InvalidTransactionException(
                    "Somente transações aprovadas podem ser compensadas."
            );
        }

        if (original.getOriginalTransactionId() != null) {
            throw new InvalidTransactionException(
                    "Uma transação compensatória não pode ser compensada novamente."
            );
        }

        if (status == null) {
            throw new InvalidTransactionException(
                    "O status da compensação é obrigatório."
            );
        }

        TransactionType compensationType;
        UUID ownerId;
        UUID destinationUserId = null;

        switch (original.getType()) {
            case DEPOSIT -> {
                compensationType = TransactionType.WITHDRAWAL;
                ownerId = original.getOwnerId();
            }
            case WITHDRAWAL, PURCHASE -> {
                compensationType = TransactionType.DEPOSIT;
                ownerId = original.getOwnerId();
            }
            case TRANSFER -> {
                compensationType = TransactionType.TRANSFER;
                ownerId = original.getDestinationUserId();
                destinationUserId = original.getOwnerId();
            }
            default -> throw new InvalidTransactionException(
                    "Tipo de transação não permitido para compensação."
            );
        }

        return new Transaction(
                UUID.randomUUID(),
                ownerId,
                destinationUserId,
                compensationType,
                original.getAmount(),
                LocalDateTime.now(),
                "Compensação da transação " + original.getId(),
                original.getCategory(),
                status,
                rejectionReason,
                original.getId(),
                original.getUsdExchangeRate(),
                original.getUsdQuoteDate(),
                original.getEurExchangeRate(),
                original.getEurQuoteDate()
        );
    }
}
