package com.gerenciar.financas.transactions.adapter.out.persistence.mapper;

import com.gerenciar.financas.transactions.adapter.out.persistence.entity.TransactionEntity;
import com.gerenciar.financas.transactions.core.model.Transaction;
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
                transaction.getOriginalTransactionId(),
                transaction.getUsdExchangeRate(),
                transaction.getUsdQuoteDate(),
                transaction.getEurExchangeRate(),
                transaction.getEurQuoteDate(),
                transaction.getBalanceBefore(),
                transaction.getBalanceAfter()
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
                entity.getOriginalTransactionId(),
                entity.getUsdExchangeRate(),
                entity.getUsdQuoteDate(),
                entity.getEurExchangeRate(),
                entity.getEurQuoteDate(),
                entity.getBalanceBefore(),
                entity.getBalanceAfter()
        );
    }
}
