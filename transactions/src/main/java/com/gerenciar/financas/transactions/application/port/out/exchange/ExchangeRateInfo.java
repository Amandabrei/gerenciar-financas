package com.gerenciar.financas.transactions.application.port.out.exchange;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeRateInfo(
        String currency,
        BigDecimal rate,
        LocalDate quoteDate
) {
}