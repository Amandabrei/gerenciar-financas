package com.gerenciar.financas.transactions.adapter.out.persistence;

import com.gerenciar.financas.transactions.adapter.out.persistence.entity.TransactionEntity;
import com.gerenciar.financas.transactions.adapter.out.persistence.mapper.TransactionMapper;
import com.gerenciar.financas.transactions.adapter.out.persistence.repository.SpringDataTransactionRepository;
import com.gerenciar.financas.transactions.domain.enums.TransactionStatus;
import com.gerenciar.financas.transactions.domain.enums.TransactionType;
import com.gerenciar.financas.transactions.domain.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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
                null
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

        verify(repository).save(any(TransactionEntity.class));
    }

    @Test
    void shouldFindTransactionById() {
        when(repository.findById(transactionId))
                .thenReturn(Optional.of(entity));

        Optional<Transaction> result = adapter.findById(transactionId);

        assertTrue(result.isPresent());
        assertEquals(transactionId, result.get().getId());

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
