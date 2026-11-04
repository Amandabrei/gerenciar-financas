package com.gerenciar.financas.transactions.adapter.out.persistence.repository;

import com.gerenciar.financas.transactions.adapter.out.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataTransactionRepository
        extends JpaRepository<TransactionEntity, UUID> {

    List<TransactionEntity> findAllByOwnerId(UUID ownerId);

    boolean existsByOriginalTransactionId(UUID originalTransactionId);
}
