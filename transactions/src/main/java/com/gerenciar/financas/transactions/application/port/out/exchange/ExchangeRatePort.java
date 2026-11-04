package com.gerenciar.financas.transactions.application.port.out.exchange;

import java.util.Optional;

public interface ExchangeRatePort {

    Optional<ExchangeRateInfo> getExchangeRate(String currency);
}