package com.gerenciar.financas.transactions.adapter.in.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountBalanceResponse(
        UUID userId,
        BigDecimal balance
) {
}