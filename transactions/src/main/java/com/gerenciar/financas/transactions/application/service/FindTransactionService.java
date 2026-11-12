package com.gerenciar.financas.transactions.application.service;

import com.gerenciar.financas.transactions.application.port.in.FindTransactionUseCase;
import com.gerenciar.financas.transactions.application.port.out.TransactionRepositoryPort;
import com.gerenciar.financas.transactions.core.enums.TransactionType;
import com.gerenciar.financas.transactions.core.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FindTransactionService implements FindTransactionUseCase {

    private final TransactionRepositoryPort transactionRepositoryPort;

    public FindTransactionService(
            TransactionRepositoryPort transactionRepositoryPort) {
        this.transactionRepositoryPort = transactionRepositoryPort;
    }

    @Override
    public Optional<Transaction> findById(UUID transactionId) {
        return transactionRepositoryPort.findById(transactionId);
    }

    @Override
    public List<Transaction> findAllByOwnerId(
            UUID ownerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            TransactionType type) {

        return transactionRepositoryPort.findAllByOwnerId(ownerId)
                .stream()
                .filter(transaction ->
                        startDate == null ||
                                !transaction.getCreatedAt().isBefore(startDate))
                .filter(transaction ->
                        endDate == null ||
                                !transaction.getCreatedAt().isAfter(endDate))
                .filter(transaction ->
                        type == null || transaction.getType() == type)
                .toList();
    }
}
