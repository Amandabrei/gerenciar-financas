package com.gerenciar.financas.transactions.application.service;

import com.gerenciar.financas.transactions.application.port.out.UserValidationPort;
import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialInfo;
import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialPort;
import com.gerenciar.financas.transactions.domain.exception.AccountNotFoundException;
import com.gerenciar.financas.transactions.domain.exception.UserNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FindAccountBalanceServiceTest {

    private AccountFinancialPort accountFinancialPort;
    private UserValidationPort userValidationPort;
    private FindAccountBalanceService service;

    @BeforeEach
    void setUp() {
        accountFinancialPort = mock(AccountFinancialPort.class);
        userValidationPort = mock(UserValidationPort.class);

        service = new FindAccountBalanceService(
                accountFinancialPort,
                userValidationPort
        );
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

        when(userValidationPort.userExists(userId)).thenReturn(true);
        when(accountFinancialPort.getAccountFinancialInfo(userId))
                .thenReturn(financialInfo);

        BigDecimal result = service.findBalance(userId);

        assertEquals(new BigDecimal("1500.00"), result);
        verify(userValidationPort).userExists(userId);
        verify(accountFinancialPort).getAccountFinancialInfo(userId);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(userValidationPort.userExists(userId)).thenReturn(false);

        assertThrows(
                UserNotFoundException.class,
                () -> service.findBalance(userId)
        );

        verify(accountFinancialPort, never()).getAccountFinancialInfo(any());
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(userValidationPort.userExists(userId)).thenReturn(true);
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

        when(userValidationPort.userExists(userId)).thenReturn(true);
        when(accountFinancialPort.getAccountFinancialInfo(userId))
                .thenReturn(financialInfo);

        assertThrows(
                AccountNotFoundException.class,
                () -> service.findBalance(userId)
        );
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        assertThrows(
                UserNotFoundException.class,
                () -> service.findBalance(null)
        );

        verifyNoInteractions(userValidationPort, accountFinancialPort);
    }
}
