package com.gerenciar.financas.transactions.application.service;

import com.gerenciar.financas.transactions.application.port.in.FindAccountBalanceUseCase;
import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialInfo;
import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialPort;
import com.gerenciar.financas.transactions.domain.exception.AccountNotFoundException;

import java.math.BigDecimal;
import java.util.UUID;

public class FindAccountBalanceService implements FindAccountBalanceUseCase {

    private final AccountFinancialPort accountFinancialPort;

    public FindAccountBalanceService(AccountFinancialPort accountFinancialPort) {
        this.accountFinancialPort = accountFinancialPort;
    }

    @Override
    public BigDecimal findBalance(UUID userId) {

        AccountFinancialInfo financialInfo =
                accountFinancialPort.getAccountFinancialInfo(userId);

        if (financialInfo == null || financialInfo.balance() == null) {
            throw new AccountNotFoundException(
                    "Conta financeira não encontrada para o usuário informado."
            );
        }

        return financialInfo.balance();
    }
}