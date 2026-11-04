package com.gerenciar.financas.transactions.adapter.out.persistence.entity;

import com.gerenciar.financas.transactions.domain.enums.TransactionStatus;
import com.gerenciar.financas.transactions.domain.enums.TransactionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "financial_transactions")
public class TransactionEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID ownerId;

    private UUID destinationUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status;

    @Column(length = 500)
    private String rejectionReason;

    private UUID originalTransactionId;

    protected TransactionEntity() {
        // Construtor exigido pelo JPA
    }

    public TransactionEntity(
            UUID id,
            UUID ownerId,
            UUID destinationUserId,
            TransactionType type,
            BigDecimal amount,
            LocalDateTime createdAt,
            String description,
            String category,
            TransactionStatus status,
            String rejectionReason,
            UUID originalTransactionId
    ) {
        this.id = id;
        this.ownerId = ownerId;
        this.destinationUserId = destinationUserId;
        this.type = type;
        this.amount = amount;
        this.createdAt = createdAt;
        this.description = description;
        this.category = category;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.originalTransactionId = originalTransactionId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public UUID getDestinationUserId() {
        return destinationUserId;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public UUID getOriginalTransactionId() {
        return originalTransactionId;
    }
}
