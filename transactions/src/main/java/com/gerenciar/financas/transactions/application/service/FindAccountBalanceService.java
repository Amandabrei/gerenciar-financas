package com.gerenciar.financas.transactions.application.service;

import com.gerenciar.financas.transactions.application.port.in.FindAccountBalanceUseCase;
import com.gerenciar.financas.transactions.application.port.out.UserValidationPort;
import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialInfo;
import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialPort;
import com.gerenciar.financas.transactions.core.exception.AccountNotFoundException;
import com.gerenciar.financas.transactions.core.exception.UserNotFoundException;

import java.math.BigDecimal;
import java.util.UUID;

public class FindAccountBalanceService implements FindAccountBalanceUseCase {

    private final AccountFinancialPort accountFinancialPort;
    private final UserValidationPort userValidationPort;

    public FindAccountBalanceService(
            AccountFinancialPort accountFinancialPort,
            UserValidationPort userValidationPort) {
        this.accountFinancialPort = accountFinancialPort;
        this.userValidationPort = userValidationPort;
    }

    @Override
    public BigDecimal findBalance(UUID userId) {

        if (userId == null || !userValidationPort.userExists(userId)) {
            throw new UserNotFoundException(
                    "Usuário não encontrado para o identificador informado."
            );
        }

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
