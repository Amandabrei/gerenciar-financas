package com.gerenciar.financas.transactions.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface FindAccountBalanceUseCase {

    BigDecimal findBalance(UUID userId);
}