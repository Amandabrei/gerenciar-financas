package com.gerenciar.financas.transactions.application.service;

import com.gerenciar.financas.transactions.application.port.out.TransactionRepositoryPort;
import com.gerenciar.financas.transactions.core.enums.TransactionStatus;
import com.gerenciar.financas.transactions.core.enums.TransactionType;
import com.gerenciar.financas.transactions.core.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindTransactionServiceTest {

    @Mock
    private TransactionRepositoryPort transactionRepositoryPort;

    @InjectMocks
    private FindTransactionService findTransactionService;

    private UUID ownerId;
    private Transaction deposit;
    private Transaction withdrawal;
    private Transaction purchase;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();

        deposit = createTransaction(
                TransactionType.DEPOSIT,
                LocalDateTime.of(2026, 11, 2, 10, 0),
                "Depósito"
        );

        withdrawal = createTransaction(
                TransactionType.WITHDRAWAL,
                LocalDateTime.of(2026, 11, 3, 14, 0),
                "Saque"
        );

        purchase = createTransaction(
                TransactionType.PURCHASE,
                LocalDateTime.of(2026, 11, 4, 16, 0),
                "Compra"
        );
    }

    @Test
    void shouldFindTransactionById() {
        when(transactionRepositoryPort.findById(deposit.getId()))
                .thenReturn(Optional.of(deposit));

        Optional<Transaction> result =
                findTransactionService.findById(deposit.getId());

        assertTrue(result.isPresent());
        assertEquals(deposit.getId(), result.get().getId());

        verify(transactionRepositoryPort).findById(deposit.getId());
    }

    @Test
    void shouldReturnEmptyWhenTransactionDoesNotExist() {
        UUID transactionId = UUID.randomUUID();

        when(transactionRepositoryPort.findById(transactionId))
                .thenReturn(Optional.empty());

        Optional<Transaction> result =
                findTransactionService.findById(transactionId);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindAllTransactionsByOwnerId() {
        when(transactionRepositoryPort.findAllByOwnerId(ownerId))
                .thenReturn(List.of(deposit, withdrawal, purchase));

        List<Transaction> result =
                findTransactionService.findAllByOwnerId(
                        ownerId, null, null, null
                );

        assertEquals(3, result.size());
    }

    @Test
    void shouldFilterTransactionsByDatePeriod() {
        when(transactionRepositoryPort.findAllByOwnerId(ownerId))
                .thenReturn(List.of(deposit, withdrawal, purchase));

        List<Transaction> result =
                findTransactionService.findAllByOwnerId(
                        ownerId,
                        LocalDateTime.of(2026, 11, 3, 0, 0),
                        LocalDateTime.of(2026, 11, 4, 23, 59),
                        null
                );

        assertEquals(2, result.size());
        assertTrue(result.contains(withdrawal));
        assertTrue(result.contains(purchase));
    }

    @Test
    void shouldFilterTransactionsByType() {
        when(transactionRepositoryPort.findAllByOwnerId(ownerId))
                .thenReturn(List.of(deposit, withdrawal, purchase));

        List<Transaction> result =
                findTransactionService.findAllByOwnerId(
                        ownerId, null, null, TransactionType.DEPOSIT
                );

        assertEquals(1, result.size());
        assertEquals(TransactionType.DEPOSIT, result.get(0).getType());
    }

    @Test
    void shouldReturnEmptyListWhenNoTransactionMatchesFilters() {
        when(transactionRepositoryPort.findAllByOwnerId(ownerId))
                .thenReturn(List.of(deposit, withdrawal, purchase));

        List<Transaction> result =
                findTransactionService.findAllByOwnerId(
                        ownerId, null, null, TransactionType.TRANSFER
                );

        assertTrue(result.isEmpty());
    }

    private Transaction createTransaction(
            TransactionType type,
            LocalDateTime createdAt,
            String description) {

        return new Transaction(
                UUID.randomUUID(),
                ownerId,
                null,
                type,
                new BigDecimal("100.00"),
                createdAt,
                description,
                type == TransactionType.PURCHASE ? "ALIMENTACAO" : null,
                TransactionStatus.APPROVED,
                null,
                null,
                new BigDecimal("5.432100"),
                LocalDate.of(2026, 11, 4),
                new BigDecimal("6.123400"),
                LocalDate.of(2026, 11, 4)
        );
    }
}
