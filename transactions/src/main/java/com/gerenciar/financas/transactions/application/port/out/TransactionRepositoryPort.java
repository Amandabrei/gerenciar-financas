package com.gerenciar.financas.transactions.application.port.out;

import com.gerenciar.financas.transactions.domain.model.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepositoryPort {

    Transaction save(Transaction transaction);

    Optional<Transaction> findById(UUID id);

    List<Transaction> findAllByOwnerId(UUID ownerId);

    boolean existsByOriginalTransactionId(UUID originalTransactionId);
}