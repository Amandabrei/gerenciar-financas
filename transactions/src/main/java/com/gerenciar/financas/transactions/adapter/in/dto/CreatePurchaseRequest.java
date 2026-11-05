package com.gerenciar.financas.transactions.adapter.in.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePurchaseRequest(
        @NotNull UUID ownerId,
        @NotNull @Positive @Digits(integer = 17, fraction = 2)
        BigDecimal amount,
        @NotBlank @Size(max = 500)
        String description,
        @NotBlank @Size(max = 100)
        String category
) {}