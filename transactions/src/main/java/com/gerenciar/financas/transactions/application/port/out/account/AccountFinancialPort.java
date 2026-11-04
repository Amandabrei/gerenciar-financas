package com.gerenciar.financas.transactions.application.port.out.account;

import java.util.UUID;

public interface AccountFinancialPort {

    AccountFinancialInfo getAccountFinancialInfo(UUID userId);
}
