package com.gerenciar.financas.transactions.application.port.out.account;

import java.math.BigDecimal;

public record AccountFinancialInfo(
        BigDecimal balance,
        BigDecimal dailyLimit,
        BigDecimal transactionLimit,
        BigDecimal dailySpent
) {
}
