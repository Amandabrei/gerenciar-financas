package com.gerenciar.financas.transactions.adapter.in.dto;

import com.gerenciar.financas.transactions.domain.enums.TransactionStatus;
import com.gerenciar.financas.transactions.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID ownerId,
        UUID destinationUserId,
        TransactionType type,
        BigDecimal amount,
        LocalDateTime createdAt,
        String description,
        String category,
        TransactionStatus status,
        String rejectionReason
) {
}
