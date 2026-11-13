package com.gerenciar.financas.transactions.application.service;

import com.gerenciar.financas.transactions.application.port.out.TransactionRepositoryPort;
import com.gerenciar.financas.transactions.application.port.out.UserValidationPort;
import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialInfo;
import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialPort;
import com.gerenciar.financas.transactions.application.port.out.exchange.ExchangeRatePort;
import com.gerenciar.financas.transactions.core.enums.TransactionStatus;
import com.gerenciar.financas.transactions.core.enums.TransactionType;
import com.gerenciar.financas.transactions.core.exception.InvalidTransactionException;
import com.gerenciar.financas.transactions.core.exception.UserNotFoundException;
import com.gerenciar.financas.transactions.core.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateTransactionServiceTest {

    private TransactionRepositoryPort transactionRepositoryPort;
    private UserValidationPort userValidationPort;
    private AccountFinancialPort accountFinancialPort;
    private ExchangeRatePort exchangeRatePort;
    private Clock clock;

    private CreateTransactionService service;

    @BeforeEach
    void setUp() {
        transactionRepositoryPort = mock(TransactionRepositoryPort.class);
        userValidationPort = mock(UserValidationPort.class);
        accountFinancialPort = mock(AccountFinancialPort.class);
        exchangeRatePort = mock(ExchangeRatePort.class);

        clock = Clock.fixed(
                Instant.parse("2026-11-13T02:30:00Z"),
                ZoneId.of("America/Sao_Paulo")
        );

        service = new CreateTransactionService(
                transactionRepositoryPort,
                userValidationPort,
                accountFinancialPort,
                exchangeRatePort,
                clock
        );
    }

    @Test
    void shouldCreateApprovedDeposit() {
        UUID ownerId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("500.00");

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

        Transaction result = service.createDeposit(
                ownerId, amount, "Depósito inicial"
        );

        assertNotNull(result);
        assertEquals(ownerId, result.getOwnerId());
        assertEquals(TransactionType.DEPOSIT, result.getType());
        assertEquals(TransactionStatus.APPROVED, result.getStatus());
        assertEquals(amount, result.getAmount());
        assertEquals(new BigDecimal("1000.00"), result.getBalanceBefore());
        assertEquals(new BigDecimal("1500.00"), result.getBalanceAfter());

        verify(accountFinancialPort).getAccountFinancialInfo(ownerId);
        verify(accountFinancialPort).updateBalance(
                ownerId, new BigDecimal("1500.00")
        );
        verify(transactionRepositoryPort).save(any(Transaction.class));
    }

    @Test
    void shouldNotCreateDepositWhenUserDoesNotExist() {
        UUID ownerId = UUID.randomUUID();

        when(userValidationPort.userExists(ownerId)).thenReturn(false);

        assertThrows(
                UserNotFoundException.class,
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
        assertEquals(new BigDecimal("1000.00"), result.getBalanceBefore());
        assertEquals(new BigDecimal("900.00"), result.getBalanceAfter());

        verify(accountFinancialPort).getAccountFinancialInfo(ownerId);
        verify(accountFinancialPort).updateBalance(
                ownerId, new BigDecimal("900.00")
        );
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
        assertEquals(new BigDecimal("50.00"), result.getBalanceBefore());
        assertEquals(new BigDecimal("50.00"), result.getBalanceAfter());

        verify(accountFinancialPort).getAccountFinancialInfo(ownerId);
        verify(accountFinancialPort, never())
                .updateBalance(any(UUID.class), any(BigDecimal.class));
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
                "Dados financeiros indisponíveis. Operação não aprovada.",
                result.getRejectionReason()
        );
        assertNull(result.getBalanceBefore());
        assertNull(result.getBalanceAfter());

        verify(accountFinancialPort).getAccountFinancialInfo(ownerId);
        verify(accountFinancialPort, never())
                .updateBalance(any(UUID.class), any(BigDecimal.class));
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

        when(accountFinancialPort.getAccountFinancialInfo(destinationUserId))
                .thenReturn(new AccountFinancialInfo(
                        new BigDecimal("500.00"),
                        new BigDecimal("2000.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("0.00")
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
        assertEquals(new BigDecimal("1000.00"), result.getBalanceBefore());
        assertEquals(new BigDecimal("900.00"), result.getBalanceAfter());

        verify(accountFinancialPort).updateBalance(
                ownerId, new BigDecimal("900.00")
        );
        verify(accountFinancialPort).updateBalance(
                destinationUserId, new BigDecimal("600.00")
        );
        verify(transactionRepositoryPort).save(any(Transaction.class));
    }

    @Test
    void shouldNotCreateTransferWhenDestinationUserDoesNotExist() {
        UUID ownerId = UUID.randomUUID();
        UUID destinationUserId = UUID.randomUUID();

        when(userValidationPort.userExists(ownerId)).thenReturn(true);
        when(userValidationPort.userExists(destinationUserId)).thenReturn(false);

        assertThrows(
                UserNotFoundException.class,
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
    void shouldNotCreateTransferToSameUser() {
        UUID ownerId = UUID.randomUUID();

        when(userValidationPort.userExists(ownerId)).thenReturn(true);

        assertThrows(
                InvalidTransactionException.class,
                () -> service.createTransfer(
                        ownerId,
                        ownerId,
                        new BigDecimal("100.00"),
                        "Transferência para o próprio usuário"
                )
        );

        verify(userValidationPort, times(2)).userExists(ownerId);
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
        assertEquals(new BigDecimal("1000.00"), result.getBalanceBefore());
        assertEquals(new BigDecimal("850.00"), result.getBalanceAfter());

        verify(accountFinancialPort).updateBalance(
                ownerId, new BigDecimal("850.00")
        );
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
        assertEquals(new BigDecimal("1000.00"), result.getBalanceBefore());
        assertEquals(new BigDecimal("1000.00"), result.getBalanceAfter());

        verify(accountFinancialPort, never())
                .updateBalance(any(UUID.class), any(BigDecimal.class));
        verify(transactionRepositoryPort).save(any(Transaction.class));
    }

    @Test
    void shouldApproveWithdrawalWhenMockDailySpentIsNotCounted() {
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

        assertEquals(TransactionStatus.APPROVED, result.getStatus());
        verify(accountFinancialPort).updateBalance(
                ownerId, new BigDecimal("800.00")
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
        assertEquals(new BigDecimal("300.00"), result.getBalanceBefore());
        assertEquals(new BigDecimal("300.00"), result.getBalanceAfter());

        verify(accountFinancialPort, never())
                .updateBalance(any(UUID.class), any(BigDecimal.class));
    }

    @Test
    void shouldApproveWithdrawalWhenPreviousApprovedTransactionsAreWithinDailyLimit() {
        UUID ownerId = UUID.randomUUID();

        Transaction previousWithdrawal = new Transaction(
                UUID.randomUUID(),
                ownerId,
                null,
                TransactionType.WITHDRAWAL,
                new BigDecimal("150.00"),
                java.time.LocalDateTime.now(clock),
                "Saque anterior",
                "WITHDRAWAL",
                TransactionStatus.APPROVED,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(userValidationPort.userExists(ownerId)).thenReturn(true);

        when(transactionRepositoryPort.findAllByOwnerId(ownerId))
                .thenReturn(List.of(previousWithdrawal));

        when(accountFinancialPort.getAccountFinancialInfo(ownerId))
                .thenReturn(new AccountFinancialInfo(
                        new BigDecimal("1000.00"),
                        new BigDecimal("300.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00")
                ));

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.createWithdrawal(
                ownerId, new BigDecimal("100.00"), "Novo saque"
        );

        assertEquals(TransactionStatus.APPROVED, result.getStatus());
        verify(accountFinancialPort).updateBalance(
                ownerId, new BigDecimal("900.00")
        );
    }

    @Test
    void shouldRejectWithdrawalWhenPreviousApprovedTransactionsExceedDailyLimit() {
        UUID ownerId = UUID.randomUUID();

        Transaction previousWithdrawal = new Transaction(
                UUID.randomUUID(),
                ownerId,
                null,
                TransactionType.WITHDRAWAL,
                new BigDecimal("250.00"),
                java.time.LocalDateTime.now(clock),
                "Saque anterior",
                "WITHDRAWAL",
                TransactionStatus.APPROVED,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(userValidationPort.userExists(ownerId)).thenReturn(true);

        when(transactionRepositoryPort.findAllByOwnerId(ownerId))
                .thenReturn(List.of(previousWithdrawal));

        when(accountFinancialPort.getAccountFinancialInfo(ownerId))
                .thenReturn(new AccountFinancialInfo(
                        new BigDecimal("1000.00"),
                        new BigDecimal("300.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00")
                ));

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.createWithdrawal(
                ownerId, new BigDecimal("100.00"), "Novo saque"
        );

        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertEquals(
                "O limite diário foi ultrapassado.",
                result.getRejectionReason()
        );

        verify(accountFinancialPort, never())
                .updateBalance(any(UUID.class), any(BigDecimal.class));
        verify(transactionRepositoryPort).save(any(Transaction.class));
    }

    @Test
    void shouldIgnoreRejectedTransactionsWhenCalculatingDailySpent() {
        UUID ownerId = UUID.randomUUID();

        Transaction rejectedWithdrawal = new Transaction(
                UUID.randomUUID(),
                ownerId,
                null,
                TransactionType.WITHDRAWAL,
                new BigDecimal("250.00"),
                java.time.LocalDateTime.now(clock),
                "Saque rejeitado",
                "WITHDRAWAL",
                TransactionStatus.REJECTED,
                "Saldo insuficiente.",
                null,
                null,
                null,
                null,
                null
        );

        when(userValidationPort.userExists(ownerId)).thenReturn(true);

        when(transactionRepositoryPort.findAllByOwnerId(ownerId))
                .thenReturn(List.of(rejectedWithdrawal));

        when(accountFinancialPort.getAccountFinancialInfo(ownerId))
                .thenReturn(new AccountFinancialInfo(
                        new BigDecimal("1000.00"),
                        new BigDecimal("300.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00")
                ));

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.createWithdrawal(
                ownerId, new BigDecimal("100.00"), "Novo saque"
        );

        assertEquals(TransactionStatus.APPROVED, result.getStatus());
        verify(accountFinancialPort).updateBalance(
                ownerId, new BigDecimal("900.00")
        );
    }

    @Test
    void shouldIgnoreApprovedDepositsWhenCalculatingDailySpent() {
        UUID ownerId = UUID.randomUUID();

        Transaction previousDeposit = new Transaction(
                UUID.randomUUID(),
                ownerId,
                null,
                TransactionType.DEPOSIT,
                new BigDecimal("250.00"),
                java.time.LocalDateTime.now(clock),
                "Depósito anterior",
                "DEPOSIT",
                TransactionStatus.APPROVED,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(userValidationPort.userExists(ownerId)).thenReturn(true);

        when(transactionRepositoryPort.findAllByOwnerId(ownerId))
                .thenReturn(List.of(previousDeposit));

        when(accountFinancialPort.getAccountFinancialInfo(ownerId))
                .thenReturn(new AccountFinancialInfo(
                        new BigDecimal("1000.00"),
                        new BigDecimal("300.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00")
                ));

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.createWithdrawal(
                ownerId, new BigDecimal("100.00"), "Novo saque"
        );

        assertEquals(TransactionStatus.APPROVED, result.getStatus());
        verify(accountFinancialPort).updateBalance(
                ownerId, new BigDecimal("900.00")
        );
    }

    @Test
    void shouldApproveTransferWhenPreviousApprovedTransactionsAreWithinDailyLimit() {
        UUID ownerId = UUID.randomUUID();
        UUID previousDestinationUserId = UUID.randomUUID();
        UUID newDestinationUserId = UUID.randomUUID();

        Transaction previousTransfer = new Transaction(
                UUID.randomUUID(),
                ownerId,
                previousDestinationUserId,
                TransactionType.TRANSFER,
                new BigDecimal("150.00"),
                java.time.LocalDateTime.now(clock),
                "Transferência anterior",
                "TRANSFER",
                TransactionStatus.APPROVED,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(userValidationPort.userExists(ownerId)).thenReturn(true);
        when(userValidationPort.userExists(previousDestinationUserId)).thenReturn(true);
        when(userValidationPort.userExists(newDestinationUserId)).thenReturn(true);

        when(transactionRepositoryPort.findAllByOwnerId(ownerId))
                .thenReturn(List.of(previousTransfer));

        when(accountFinancialPort.getAccountFinancialInfo(ownerId))
                .thenReturn(new AccountFinancialInfo(
                        new BigDecimal("1000.00"),
                        new BigDecimal("300.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00")
                ));

        when(accountFinancialPort.getAccountFinancialInfo(newDestinationUserId))
                .thenReturn(new AccountFinancialInfo(
                        new BigDecimal("500.00"),
                        new BigDecimal("2000.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("0.00")
                ));

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.createTransfer(
                ownerId,
                newDestinationUserId,
                new BigDecimal("100.00"),
                "Transferência teste"
        );

        assertEquals(TransactionStatus.APPROVED, result.getStatus());

        verify(accountFinancialPort).updateBalance(
                ownerId, new BigDecimal("900.00")
        );
        verify(accountFinancialPort).updateBalance(
                newDestinationUserId, new BigDecimal("600.00")
        );
    }

    @Test
    void shouldApprovePurchaseWhenPreviousApprovedTransactionsAreWithinDailyLimit() {
        UUID ownerId = UUID.randomUUID();

        Transaction previousPurchase = new Transaction(
                UUID.randomUUID(),
                ownerId,
                null,
                TransactionType.PURCHASE,
                new BigDecimal("150.00"),
                java.time.LocalDateTime.now(clock),
                "Compra anterior",
                "ALIMENTACAO",
                TransactionStatus.APPROVED,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(userValidationPort.userExists(ownerId)).thenReturn(true);

        when(transactionRepositoryPort.findAllByOwnerId(ownerId))
                .thenReturn(List.of(previousPurchase));

        when(accountFinancialPort.getAccountFinancialInfo(ownerId))
                .thenReturn(new AccountFinancialInfo(
                        new BigDecimal("1000.00"),
                        new BigDecimal("300.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00")
                ));

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.createPurchase(
                ownerId,
                new BigDecimal("100.00"),
                "Nova compra",
                "ALIMENTACAO"
        );

        assertEquals(TransactionStatus.APPROVED, result.getStatus());
        verify(accountFinancialPort).updateBalance(
                ownerId, new BigDecimal("900.00")
        );
    }


    @Test
    void shouldRejectWithdrawalAndRestoreBalanceWhenMockApiUpdateFails() {
        UUID ownerId = UUID.randomUUID();
        BigDecimal previousBalance = new BigDecimal("1000.00");
        BigDecimal newBalance = new BigDecimal("900.00");

        when(userValidationPort.userExists(ownerId)).thenReturn(true);

        when(accountFinancialPort.getAccountFinancialInfo(ownerId))
                .thenReturn(new AccountFinancialInfo(
                        previousBalance,
                        new BigDecimal("2000.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00")
                ));

        doThrow(new RuntimeException("MockAPI indisponível"))
                .when(accountFinancialPort)
                .updateBalance(ownerId, newBalance);

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.createWithdrawal(
                ownerId,
                new BigDecimal("100.00"),
                "Saque com falha na atualização"
        );

        assertNotNull(result);
        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertEquals(
                "Não foi possível atualizar o saldo do saque. Operação não aprovada.",
                result.getRejectionReason()
        );
        assertEquals(previousBalance, result.getBalanceBefore());
        assertEquals(previousBalance, result.getBalanceAfter());

        verify(accountFinancialPort).updateBalance(ownerId, newBalance);
        verify(accountFinancialPort).updateBalance(ownerId, previousBalance);
        verify(transactionRepositoryPort).save(any(Transaction.class));
    }

    @Test
    void shouldRestoreBalanceWhenDatabaseSaveFailsAfterBalanceUpdate() {
        UUID ownerId = UUID.randomUUID();
        BigDecimal previousBalance = new BigDecimal("1000.00");
        BigDecimal newBalance = new BigDecimal("900.00");

        when(userValidationPort.userExists(ownerId)).thenReturn(true);

        when(accountFinancialPort.getAccountFinancialInfo(ownerId))
                .thenReturn(new AccountFinancialInfo(
                        previousBalance,
                        new BigDecimal("2000.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00")
                ));

        RuntimeException databaseException =
                new RuntimeException("Falha ao salvar no PostgreSQL");

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenThrow(databaseException);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.createWithdrawal(
                        ownerId,
                        new BigDecimal("100.00"),
                        "Saque com falha no banco"
                )
        );

        assertSame(databaseException, exception);

        verify(accountFinancialPort).updateBalance(ownerId, newBalance);
        verify(accountFinancialPort).updateBalance(ownerId, previousBalance);
        verify(transactionRepositoryPort).save(any(Transaction.class));
    }

    @Test
    void shouldRestoreBothBalancesWhenDestinationUpdateFailsDuringTransfer() {
        UUID sourceUserId = UUID.randomUUID();
        UUID destinationUserId = UUID.randomUUID();

        BigDecimal sourcePreviousBalance = new BigDecimal("1000.00");
        BigDecimal sourceNewBalance = new BigDecimal("900.00");

        BigDecimal destinationPreviousBalance = new BigDecimal("500.00");
        BigDecimal destinationNewBalance = new BigDecimal("600.00");

        when(userValidationPort.userExists(sourceUserId)).thenReturn(true);
        when(userValidationPort.userExists(destinationUserId)).thenReturn(true);

        when(accountFinancialPort.getAccountFinancialInfo(sourceUserId))
                .thenReturn(new AccountFinancialInfo(
                        sourcePreviousBalance,
                        new BigDecimal("2000.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00")
                ));

        when(accountFinancialPort.getAccountFinancialInfo(destinationUserId))
                .thenReturn(new AccountFinancialInfo(
                        destinationPreviousBalance,
                        new BigDecimal("2000.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("0.00")
                ));

        doThrow(new RuntimeException("Falha ao atualizar conta de destino"))
                .when(accountFinancialPort)
                .updateBalance(destinationUserId, destinationNewBalance);

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.createTransfer(
                sourceUserId,
                destinationUserId,
                new BigDecimal("100.00"),
                "Transferência com falha"
        );

        assertNotNull(result);
        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertEquals(
                "Não foi possível atualizar os saldos da transferência. Operação não aprovada.",
                result.getRejectionReason()
        );
        assertEquals(sourcePreviousBalance, result.getBalanceBefore());
        assertEquals(sourcePreviousBalance, result.getBalanceAfter());

        verify(accountFinancialPort).updateBalance(
                sourceUserId, sourceNewBalance
        );

        verify(accountFinancialPort).updateBalance(
                destinationUserId, destinationNewBalance
        );

        verify(accountFinancialPort).updateBalance(
                destinationUserId, destinationPreviousBalance
        );

        verify(accountFinancialPort).updateBalance(
                sourceUserId, sourcePreviousBalance
        );

        verify(transactionRepositoryPort).save(any(Transaction.class));
    }

    @Test
    void shouldRestoreBothBalancesWhenDatabaseSaveFailsAfterTransferUpdates() {
        UUID sourceUserId = UUID.randomUUID();
        UUID destinationUserId = UUID.randomUUID();

        BigDecimal sourcePreviousBalance = new BigDecimal("1000.00");
        BigDecimal sourceNewBalance = new BigDecimal("900.00");

        BigDecimal destinationPreviousBalance = new BigDecimal("500.00");
        BigDecimal destinationNewBalance = new BigDecimal("600.00");

        when(userValidationPort.userExists(sourceUserId)).thenReturn(true);
        when(userValidationPort.userExists(destinationUserId)).thenReturn(true);

        when(accountFinancialPort.getAccountFinancialInfo(sourceUserId))
                .thenReturn(new AccountFinancialInfo(
                        sourcePreviousBalance,
                        new BigDecimal("2000.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00")
                ));

        when(accountFinancialPort.getAccountFinancialInfo(destinationUserId))
                .thenReturn(new AccountFinancialInfo(
                        destinationPreviousBalance,
                        new BigDecimal("2000.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("0.00")
                ));

        RuntimeException databaseException =
                new RuntimeException("Falha ao salvar transferência");

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenThrow(databaseException);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.createTransfer(
                        sourceUserId,
                        destinationUserId,
                        new BigDecimal("100.00"),
                        "Transferência com falha no banco"
                )
        );

        assertSame(databaseException, exception);

        verify(accountFinancialPort).updateBalance(
                sourceUserId, sourceNewBalance
        );

        verify(accountFinancialPort).updateBalance(
                destinationUserId, destinationNewBalance
        );

        verify(accountFinancialPort).updateBalance(
                destinationUserId, destinationPreviousBalance
        );

        verify(accountFinancialPort).updateBalance(
                sourceUserId, sourcePreviousBalance
        );

        verify(transactionRepositoryPort).save(any(Transaction.class));
    }


    @Test
    void shouldUseSaoPauloDateWhenCalculatingDailyLimit() {
        UUID ownerId = UUID.randomUUID();

        Transaction previousWithdrawal = new Transaction(
                UUID.randomUUID(),
                ownerId,
                null,
                TransactionType.WITHDRAWAL,
                new BigDecimal("250.00"),
                LocalDateTime.of(2026, 11, 12, 10, 0),
                "Saque anterior",
                "WITHDRAWAL",
                TransactionStatus.APPROVED,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(userValidationPort.userExists(ownerId)).thenReturn(true);

        when(transactionRepositoryPort.findAllByOwnerId(ownerId))
                .thenReturn(List.of(previousWithdrawal));

        when(accountFinancialPort.getAccountFinancialInfo(ownerId))
                .thenReturn(new AccountFinancialInfo(
                        new BigDecimal("1000.00"),
                        new BigDecimal("300.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00")
                ));

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.createWithdrawal(
                ownerId,
                new BigDecimal("100.00"),
                "Novo saque"
        );

        assertEquals(TransactionStatus.REJECTED, result.getStatus());

        assertEquals(
                "O limite diário foi ultrapassado.",
                result.getRejectionReason()
        );

        verify(accountFinancialPort, never())
                .updateBalance(any(UUID.class), any(BigDecimal.class));

        verify(transactionRepositoryPort).save(any(Transaction.class));
    }

    @Test
    void shouldKeepBalanceSnapshotsNullWhenCompensationFails() {
        UUID ownerId = UUID.randomUUID();

        BigDecimal previousBalance = new BigDecimal("1000.00");
        BigDecimal newBalance = new BigDecimal("900.00");

        when(userValidationPort.userExists(ownerId)).thenReturn(true);

        when(accountFinancialPort.getAccountFinancialInfo(ownerId))
                .thenReturn(new AccountFinancialInfo(
                        previousBalance,
                        new BigDecimal("2000.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00")
                ));

        doThrow(new RuntimeException("Falha na atualização"))
                .when(accountFinancialPort)
                .updateBalance(ownerId, newBalance);

        doThrow(new RuntimeException("Falha na compensação"))
                .when(accountFinancialPort)
                .updateBalance(ownerId, previousBalance);

        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.createWithdrawal(
                ownerId,
                new BigDecimal("100.00"),
                "Saque com falha na compensação"
        );

        assertNotNull(result);
        assertEquals(TransactionStatus.REJECTED, result.getStatus());

        assertNull(result.getBalanceBefore());
        assertNull(result.getBalanceAfter());

        verify(accountFinancialPort).updateBalance(ownerId, newBalance);
        verify(accountFinancialPort).updateBalance(ownerId, previousBalance);

        verify(transactionRepositoryPort).save(any(Transaction.class));
    }
}
