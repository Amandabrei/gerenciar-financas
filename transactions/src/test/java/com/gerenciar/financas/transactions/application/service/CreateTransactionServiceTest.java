package com.gerenciar.financas.transactions.application.service;

import com.gerenciar.financas.transactions.application.port.out.TransactionRepositoryPort;
import com.gerenciar.financas.transactions.application.port.out.UserValidationPort;
import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialInfo;
import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialPort;
import com.gerenciar.financas.transactions.application.port.out.exchange.ExchangeRatePort;
import com.gerenciar.financas.transactions.domain.enums.TransactionStatus;
import com.gerenciar.financas.transactions.domain.enums.TransactionType;
import com.gerenciar.financas.transactions.domain.exception.InvalidTransactionException;
import com.gerenciar.financas.transactions.domain.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateTransactionServiceTest {

    private TransactionRepositoryPort transactionRepositoryPort;
    private UserValidationPort userValidationPort;
    private AccountFinancialPort accountFinancialPort;
    private ExchangeRatePort exchangeRatePort;

    private CreateTransactionService service;

    @BeforeEach
    void setUp() {
        transactionRepositoryPort = mock(TransactionRepositoryPort.class);
        userValidationPort = mock(UserValidationPort.class);
        accountFinancialPort = mock(AccountFinancialPort.class);
        exchangeRatePort = mock(ExchangeRatePort.class);

        service = new CreateTransactionService(
                transactionRepositoryPort,
                userValidationPort,
                accountFinancialPort,
                exchangeRatePort
        );
    }

    @Test
    void shouldCreateApprovedDeposit() {
        UUID ownerId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("500.00");

        when(userValidationPort.userExists(ownerId)).thenReturn(true);

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.createDeposit(
                ownerId,
                amount,
                "Depósito inicial"
        );

        assertNotNull(result);
        assertEquals(ownerId, result.getOwnerId());
        assertEquals(TransactionType.DEPOSIT, result.getType());
        assertEquals(amount, result.getAmount());
        assertEquals(TransactionStatus.APPROVED, result.getStatus());

        verify(userValidationPort).userExists(ownerId);
        verify(transactionRepositoryPort).save(any(Transaction.class));
        verifyNoInteractions(accountFinancialPort);
    }

    @Test
    void shouldNotCreateDepositWhenUserDoesNotExist() {
        UUID ownerId = UUID.randomUUID();

        when(userValidationPort.userExists(ownerId)).thenReturn(false);

        assertThrows(
                InvalidTransactionException.class,
                () -> service.createDeposit(
                        ownerId,
                        new BigDecimal("500.00"),
                        "Depósito inicial"
                )
        );

        verify(userValidationPort).userExists(ownerId);
        verify(transactionRepositoryPort, never()).save(any(Transaction.class));
        verifyNoInteractions(accountFinancialPort);
    }

    @Test
    void shouldCreateApprovedWithdrawalWhenBalanceAndLimitsAreAvailable() {
        UUID ownerId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.00");

        when(userValidationPort.userExists(ownerId)).thenReturn(true);

        when(accountFinancialPort.getAccountFinancialInfo(ownerId))
                .thenReturn(new AccountFinancialInfo(
                        new BigDecimal("1000.00"),
                        new BigDecimal("2000.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00")
                ));

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.createWithdrawal(
                ownerId, amount, "Retirada para teste"
        );

        assertNotNull(result);
        assertEquals(TransactionType.WITHDRAWAL, result.getType());
        assertEquals(TransactionStatus.APPROVED, result.getStatus());
        assertEquals(amount, result.getAmount());

        verify(accountFinancialPort).getAccountFinancialInfo(ownerId);
        verify(transactionRepositoryPort).save(any(Transaction.class));
    }

    @Test
    void shouldRejectWithdrawalWhenBalanceIsInsufficient() {
        UUID ownerId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.00");

        when(userValidationPort.userExists(ownerId)).thenReturn(true);

        when(accountFinancialPort.getAccountFinancialInfo(ownerId))
                .thenReturn(new AccountFinancialInfo(
                        new BigDecimal("50.00"),
                        new BigDecimal("2000.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00")
                ));

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.createWithdrawal(
                ownerId, amount, "Retirada para teste"
        );

        assertNotNull(result);
        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertEquals("Saldo insuficiente.", result.getRejectionReason());

        verify(accountFinancialPort).getAccountFinancialInfo(ownerId);
        verify(transactionRepositoryPort).save(any(Transaction.class));
    }

    @Test
    void shouldRejectWithdrawalWhenAccountApiIsUnavailable() {
        UUID ownerId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.00");

        when(userValidationPort.userExists(ownerId)).thenReturn(true);

        when(accountFinancialPort.getAccountFinancialInfo(ownerId))
                .thenThrow(new RuntimeException("MockAPI indisponível"));

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.createWithdrawal(
                ownerId, amount, "Retirada para teste"
        );

        assertNotNull(result);
        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertEquals(
                "Não foi possível consultar saldo e limites. Operação não aprovada.",
                result.getRejectionReason()
        );

        verify(accountFinancialPort).getAccountFinancialInfo(ownerId);
        verify(transactionRepositoryPort).save(any(Transaction.class));
    }

    @Test
    void shouldCreateApprovedTransfer() {
        UUID ownerId = UUID.randomUUID();
        UUID destinationUserId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.00");

        when(userValidationPort.userExists(ownerId)).thenReturn(true);
        when(userValidationPort.userExists(destinationUserId)).thenReturn(true);

        when(accountFinancialPort.getAccountFinancialInfo(ownerId))
                .thenReturn(new AccountFinancialInfo(
                        new BigDecimal("1000.00"),
                        new BigDecimal("2000.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00")
                ));

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.createTransfer(
                ownerId, destinationUserId, amount, "Transferência de teste"
        );

        assertNotNull(result);
        assertEquals(TransactionType.TRANSFER, result.getType());
        assertEquals(TransactionStatus.APPROVED, result.getStatus());
        assertEquals(ownerId, result.getOwnerId());
        assertEquals(destinationUserId, result.getDestinationUserId());

        verify(transactionRepositoryPort).save(any(Transaction.class));
    }

    @Test
    void shouldNotCreateTransferWhenDestinationUserDoesNotExist() {
        UUID ownerId = UUID.randomUUID();
        UUID destinationUserId = UUID.randomUUID();

        when(userValidationPort.userExists(ownerId)).thenReturn(true);
        when(userValidationPort.userExists(destinationUserId)).thenReturn(false);

        assertThrows(
                InvalidTransactionException.class,
                () -> service.createTransfer(
                        ownerId,
                        destinationUserId,
                        new BigDecimal("100.00"),
                        "Transferência de teste"
                )
        );

        verify(userValidationPort).userExists(ownerId);
        verify(userValidationPort).userExists(destinationUserId);
        verify(transactionRepositoryPort, never()).save(any(Transaction.class));
        verifyNoInteractions(accountFinancialPort);
    }

    @Test
    void shouldCreateApprovedPurchase() {
        UUID ownerId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("150.00");

        when(userValidationPort.userExists(ownerId)).thenReturn(true);

        when(accountFinancialPort.getAccountFinancialInfo(ownerId))
                .thenReturn(new AccountFinancialInfo(
                        new BigDecimal("1000.00"),
                        new BigDecimal("2000.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00")
                ));

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.createPurchase(
                ownerId, amount, "Compra no supermercado", "ALIMENTACAO"
        );

        assertNotNull(result);
        assertEquals(TransactionType.PURCHASE, result.getType());
        assertEquals(TransactionStatus.APPROVED, result.getStatus());
        assertEquals("ALIMENTACAO", result.getCategory());
        assertEquals(amount, result.getAmount());

        verify(transactionRepositoryPort).save(any(Transaction.class));
    }

    @Test
    void shouldNotCreatePurchaseWithoutCategory() {
        UUID ownerId = UUID.randomUUID();

        when(userValidationPort.userExists(ownerId)).thenReturn(true);

        assertThrows(
                InvalidTransactionException.class,
                () -> service.createPurchase(
                        ownerId,
                        new BigDecimal("150.00"),
                        "Compra no supermercado",
                        " "
                )
        );

        verify(transactionRepositoryPort, never()).save(any(Transaction.class));
        verifyNoInteractions(accountFinancialPort);
    }

    @Test
    void shouldRejectPurchaseWhenTransactionLimitIsExceeded() {
        UUID ownerId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("600.00");

        when(userValidationPort.userExists(ownerId)).thenReturn(true);

        when(accountFinancialPort.getAccountFinancialInfo(ownerId))
                .thenReturn(new AccountFinancialInfo(
                        new BigDecimal("1000.00"),
                        new BigDecimal("2000.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00")
                ));

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.createPurchase(
                ownerId, amount, "Compra de teste", "ELETRONICOS"
        );

        assertNotNull(result);
        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertEquals(
                "O valor ultrapassa o limite por transação.",
                result.getRejectionReason()
        );

        verify(transactionRepositoryPort).save(any(Transaction.class));
    }

    @Test
    void shouldRejectWithdrawalWhenDailyLimitIsExceeded() {
        UUID ownerId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("200.00");

        when(userValidationPort.userExists(ownerId)).thenReturn(true);

        when(accountFinancialPort.getAccountFinancialInfo(ownerId))
                .thenReturn(new AccountFinancialInfo(
                        new BigDecimal("1000.00"),
                        new BigDecimal("250.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00")
                ));

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.createWithdrawal(
                ownerId, amount, "Retirada de teste"
        );

        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertEquals(
                "O limite diário foi ultrapassado.",
                result.getRejectionReason()
        );
    }

    @Test
    void shouldRejectTransferWhenBalanceIsInsufficient() {
        UUID ownerId = UUID.randomUUID();
        UUID destinationUserId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("500.00");

        when(userValidationPort.userExists(ownerId)).thenReturn(true);
        when(userValidationPort.userExists(destinationUserId)).thenReturn(true);

        when(accountFinancialPort.getAccountFinancialInfo(ownerId))
                .thenReturn(new AccountFinancialInfo(
                        new BigDecimal("300.00"),
                        new BigDecimal("2000.00"),
                        new BigDecimal("1000.00"),
                        new BigDecimal("100.00")
                ));

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.createTransfer(
                ownerId, destinationUserId, amount, "Transferência de teste"
        );

        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertEquals("Saldo insuficiente.", result.getRejectionReason());
    }
}