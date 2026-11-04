package com.gerenciar.financas.transactions.infrastructure.config;

import com.gerenciar.financas.transactions.application.port.out.TransactionRepositoryPort;
import com.gerenciar.financas.transactions.application.port.out.UserValidationPort;
import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialPort;
import com.gerenciar.financas.transactions.application.port.out.exchange.ExchangeRatePort;
import com.gerenciar.financas.transactions.application.service.CreateTransactionService;
import com.gerenciar.financas.transactions.application.service.FindTransactionService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionServiceConfig {

    @Bean
    public CreateTransactionService createTransactionService(
            TransactionRepositoryPort transactionRepositoryPort,
            UserValidationPort userValidationPort,
            AccountFinancialPort accountFinancialPort,
            ExchangeRatePort exchangeRatePort) {

        return new CreateTransactionService(
                transactionRepositoryPort,
                userValidationPort,
                accountFinancialPort,
                exchangeRatePort
        );
    }

    @Bean
    public FindTransactionService findTransactionService(
            TransactionRepositoryPort transactionRepositoryPort) {
        return new FindTransactionService(transactionRepositoryPort);
    }
}