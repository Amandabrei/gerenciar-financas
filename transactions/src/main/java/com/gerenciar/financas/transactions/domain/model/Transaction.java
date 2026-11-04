package com.gerenciar.financas.transactions.domain.model;

import com.gerenciar.financas.transactions.domain.enums.TransactionStatus;
import com.gerenciar.financas.transactions.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {

    private final UUID id;
    private final UUID ownerId;
    private final UUID destinationUserId;
    private final TransactionType type;
    private final BigDecimal amount;
    private final LocalDateTime createdAt;
    private final String description;
    private final String category;
    private final TransactionStatus status;
    private final String rejectionReason;
    private final UUID originalTransactionId;

    public Transaction(
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
        if (ownerId == null) {
            throw new IllegalArgumentException("O usuário proprietário é obrigatório.");
        }

        if (type == null) {
            throw new IllegalArgumentException("O tipo da transação é obrigatório.");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("A data da transação é obrigatória.");
        }

        if (type == TransactionType.TRANSFER && destinationUserId == null) {
            throw new IllegalArgumentException("A transferência precisa de um usuário de destino.");
        }

        if (type != TransactionType.TRANSFER && destinationUserId != null) {
            throw new IllegalArgumentException("Somente transferências podem ter usuário de destino.");
        }

        if (type == TransactionType.PURCHASE &&
                (category == null || category.isBlank())) {
            throw new IllegalArgumentException("A compra precisa de uma categoria.");
        }

        if (status == null) {
            throw new IllegalArgumentException("O status da transação é obrigatório.");
        }

        if (status == TransactionStatus.REJECTED &&
                (rejectionReason == null || rejectionReason.isBlank())) {
            throw new IllegalArgumentException("Uma transação rejeitada precisa informar o motivo.");
        }

        this.id = id != null ? id : UUID.randomUUID();
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
