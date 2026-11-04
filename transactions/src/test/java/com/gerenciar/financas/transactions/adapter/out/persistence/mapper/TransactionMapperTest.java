package com.gerenciar.financas.transactions.adapter.out.persistence.mapper;

import com.gerenciar.financas.transactions.adapter.out.persistence.entity.TransactionEntity;
import com.gerenciar.financas.transactions.domain.enums.TransactionStatus;
import com.gerenciar.financas.transactions.domain.enums.TransactionType;
import com.gerenciar.financas.transactions.domain.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionMapperTest {

    private TransactionMapper mapper;
    private UUID transactionId;
    private UUID ownerId;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        mapper = new TransactionMapper();

        transactionId = UUID.randomUUID();
        ownerId = UUID.randomUUID();

        transaction = new Transaction(
                transactionId,
                ownerId,
                null,
                TransactionType.DEPOSIT,
                new BigDecimal("150.00"),
                LocalDateTime.of(2026, 11, 3, 10, 0),
                "Depósito inicial",
                "DEPOSIT",
                TransactionStatus.APPROVED,
                null,
                null
        );
    }

    @Test
    void shouldConvertDomainToEntity() {
        TransactionEntity entity = mapper.toEntity(transaction);

        assertNotNull(entity);
        assertEquals(transactionId, entity.getId());
        assertEquals(ownerId, entity.getOwnerId());
        assertEquals(TransactionType.DEPOSIT, entity.getType());
        assertEquals(new BigDecimal("150.00"), entity.getAmount());
        assertEquals("Depósito inicial", entity.getDescription());
        assertEquals(TransactionStatus.APPROVED, entity.getStatus());
    }

    @Test
    void shouldConvertEntityToDomain() {
        TransactionEntity entity = mapper.toEntity(transaction);

        Transaction result = mapper.toDomain(entity);

        assertNotNull(result);
        assertEquals(transactionId, result.getId());
        assertEquals(ownerId, result.getOwnerId());
        assertEquals(TransactionType.DEPOSIT, result.getType());
        assertEquals(new BigDecimal("150.00"), result.getAmount());
        assertEquals("Depósito inicial", result.getDescription());
        assertEquals(TransactionStatus.APPROVED, result.getStatus());
    }
}
