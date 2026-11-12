package com.gerenciar.financas.transactions.application.port.in;

import com.gerenciar.financas.transactions.core.enums.TransactionType;
import com.gerenciar.financas.transactions.core.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FindTransactionUseCase {

    Optional<Transaction> findById(UUID transactionId);

    List<Transaction> findAllByOwnerId(
            UUID ownerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            TransactionType type
    );
}
