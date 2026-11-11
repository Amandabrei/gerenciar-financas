package com.gerenciar.financas.transactions.application.port.out.account;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountFinancialPort {

    AccountFinancialInfo getAccountFinancialInfo(UUID userId);

    void updateBalance(UUID userId, BigDecimal newBalance);
}
