package com.gerenciar.financas.transactions.application.service;

import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialInfo;
import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialPort;
import com.gerenciar.financas.transactions.domain.exception.AccountNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FindAccountBalanceServiceTest {

    private AccountFinancialPort accountFinancialPort;
    private FindAccountBalanceService service;

    @BeforeEach
    void setUp() {
        accountFinancialPort = mock(AccountFinancialPort.class);
        service = new FindAccountBalanceService(accountFinancialPort);
    }

    @Test
    void shouldReturnAccountBalance() {
        UUID userId = UUID.randomUUID();

        AccountFinancialInfo financialInfo = new AccountFinancialInfo(
                new BigDecimal("1500.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("2000.00"),
                BigDecimal.ZERO
        );

        when(accountFinancialPort.getAccountFinancialInfo(userId))
                .thenReturn(financialInfo);

        BigDecimal result = service.findBalance(userId);

        assertEquals(new BigDecimal("1500.00"), result);
        verify(accountFinancialPort).getAccountFinancialInfo(userId);
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(accountFinancialPort.getAccountFinancialInfo(userId))
                .thenReturn(null);

        assertThrows(
                AccountNotFoundException.class,
                () -> service.findBalance(userId)
        );
    }

    @Test
    void shouldThrowExceptionWhenBalanceIsNull() {
        UUID userId = UUID.randomUUID();

        AccountFinancialInfo financialInfo = new AccountFinancialInfo(
                null,
                new BigDecimal("5000.00"),
                new BigDecimal("2000.00"),
                BigDecimal.ZERO
        );

        when(accountFinancialPort.getAccountFinancialInfo(userId))
                .thenReturn(financialInfo);

        assertThrows(
                AccountNotFoundException.class,
                () -> service.findBalance(userId)
        );
    }
}
