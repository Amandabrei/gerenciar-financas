package com.gerenciar.financas.transactions.adapter.in.dto;

import com.gerenciar.financas.transactions.core.enums.TransactionStatus;
import com.gerenciar.financas.transactions.core.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        String rejectionReason,
        BigDecimal usdExchangeRate,
        LocalDate usdQuoteDate,
        BigDecimal eurExchangeRate,
        LocalDate eurQuoteDate
) {
}
