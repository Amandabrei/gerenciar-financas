package com.gerenciar.financas.transactions.adapter.out.persistence.mapper;

import com.gerenciar.financas.transactions.adapter.out.persistence.entity.TransactionEntity;
import com.gerenciar.financas.transactions.domain.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionEntity toEntity(Transaction transaction) {
        return new TransactionEntity(
                transaction.getId(),
                transaction.getOwnerId(),
                transaction.getDestinationUserId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getCreatedAt(),
                transaction.getDescription(),
                transaction.getCategory(),
                transaction.getStatus(),
                transaction.getRejectionReason(),
                transaction.getOriginalTransactionId()
        );
    }

    public Transaction toDomain(TransactionEntity entity) {
        return new Transaction(
                entity.getId(),
                entity.getOwnerId(),
                entity.getDestinationUserId(),
                entity.getType(),
                entity.getAmount(),
                entity.getCreatedAt(),
                entity.getDescription(),
                entity.getCategory(),
                entity.getStatus(),
                entity.getRejectionReason(),
                entity.getOriginalTransactionId()
        );
    }
}
