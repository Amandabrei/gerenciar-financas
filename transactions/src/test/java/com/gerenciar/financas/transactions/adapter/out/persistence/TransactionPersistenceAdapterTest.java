package com.gerenciar.financas.transactions.adapter.out.persistence;

import com.gerenciar.financas.transactions.adapter.out.persistence.entity.TransactionEntity;
import com.gerenciar.financas.transactions.adapter.out.persistence.mapper.TransactionMapper;
import com.gerenciar.financas.transactions.adapter.out.persistence.repository.SpringDataTransactionRepository;
import com.gerenciar.financas.transactions.core.enums.TransactionStatus;
import com.gerenciar.financas.transactions.core.enums.TransactionType;
import com.gerenciar.financas.transactions.core.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionPersistenceAdapterTest {

    private SpringDataTransactionRepository repository;
    private TransactionMapper mapper;
    private TransactionPersistenceAdapter adapter;

    private UUID transactionId;
    private UUID ownerId;
    private Transaction transaction;
    private TransactionEntity entity;

    private final BigDecimal usdRate = new BigDecimal("5.432100");
    private final LocalDate usdDate = LocalDate.of(2026, 11, 4);
    private final BigDecimal eurRate = new BigDecimal("6.123400");
    private final LocalDate eurDate = LocalDate.of(2026, 11, 4);

    @BeforeEach
    void setUp() {
        repository = mock(SpringDataTransactionRepository.class);
        mapper = new TransactionMapper();
        adapter = new TransactionPersistenceAdapter(repository, mapper);

        transactionId = UUID.randomUUID();
        ownerId = UUID.randomUUID();

        transaction = new Transaction(
                transactionId,
                ownerId,
                null,
                TransactionType.DEPOSIT,
                new BigDecimal("100.00"),
                LocalDateTime.now(),
                "Depósito inicial",
                "DEPOSIT",
                TransactionStatus.APPROVED,
                null,
                null,
                usdRate,
                usdDate,
                eurRate,
                eurDate
        );

        entity = mapper.toEntity(transaction);
    }

    @Test
    void shouldSaveTransaction() {
        when(repository.save(any(TransactionEntity.class)))
                .thenReturn(entity);

        Transaction result = adapter.save(transaction);

        assertNotNull(result);
        assertEquals(transactionId, result.getId());
        assertEquals(ownerId, result.getOwnerId());
        assertEquals(new BigDecimal("100.00"), result.getAmount());

        assertEquals(usdRate, result.getUsdExchangeRate());
        assertEquals(usdDate, result.getUsdQuoteDate());
        assertEquals(eurRate, result.getEurExchangeRate());
        assertEquals(eurDate, result.getEurQuoteDate());

        verify(repository).save(any(TransactionEntity.class));
    }

    @Test
    void shouldFindTransactionById() {
        when(repository.findById(transactionId))
                .thenReturn(Optional.of(entity));

        Optional<Transaction> result = adapter.findById(transactionId);

        assertTrue(result.isPresent());
        assertEquals(transactionId, result.get().getId());
        assertEquals(usdRate, result.get().getUsdExchangeRate());
        assertEquals(usdDate, result.get().getUsdQuoteDate());
        assertEquals(eurRate, result.get().getEurExchangeRate());
        assertEquals(eurDate, result.get().getEurQuoteDate());

        verify(repository).findById(transactionId);
    }

    @Test
    void shouldReturnEmptyWhenTransactionDoesNotExist() {
        when(repository.findById(transactionId))
                .thenReturn(Optional.empty());

        Optional<Transaction> result = adapter.findById(transactionId);

        assertTrue(result.isEmpty());

        verify(repository).findById(transactionId);
    }

    @Test
    void shouldFindAllTransactionsByOwnerId() {
        when(repository.findAllByOwnerId(ownerId))
                .thenReturn(List.of(entity));

        List<Transaction> result = adapter.findAllByOwnerId(ownerId);

        assertEquals(1, result.size());
        assertEquals(ownerId, result.get(0).getOwnerId());
        assertEquals(usdRate, result.get(0).getUsdExchangeRate());
        assertEquals(eurRate, result.get(0).getEurExchangeRate());

        verify(repository).findAllByOwnerId(ownerId);
    }

    @Test
    void shouldCheckIfCompensationExists() {
        UUID originalTransactionId = UUID.randomUUID();

        when(repository.existsByOriginalTransactionId(originalTransactionId))
                .thenReturn(true);

        boolean result = adapter.existsByOriginalTransactionId(originalTransactionId);

        assertTrue(result);

        verify(repository).existsByOriginalTransactionId(originalTransactionId);
    }
}
