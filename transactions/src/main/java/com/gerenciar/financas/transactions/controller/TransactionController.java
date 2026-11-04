
package com.gerenciar.financas.transactions.adapter.in.controller;

import com.gerenciar.financas.transactions.adapter.in.dto.TransactionResponse;
import com.gerenciar.financas.transactions.application.port.in.CreateTransactionUseCase;
import com.gerenciar.financas.transactions.application.port.in.FindTransactionUseCase;
import com.gerenciar.financas.transactions.domain.enums.TransactionType;
import com.gerenciar.financas.transactions.domain.model.Transaction;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final FindTransactionUseCase findTransactionUseCase;

    public TransactionController(
            CreateTransactionUseCase createTransactionUseCase,
            FindTransactionUseCase findTransactionUseCase) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.findTransactionUseCase = findTransactionUseCase;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(@PathVariable UUID id) {
        return findTransactionUseCase.findById(id)
                .map(transaction -> ResponseEntity.ok(toResponse(transaction)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<TransactionResponse> findAllByOwnerId(
            @RequestParam UUID ownerId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) TransactionType type) {

        return findTransactionUseCase.findAllByOwnerId(
                        ownerId, startDate, endDate, type)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/deposits")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createDeposit(
            @Valid @RequestBody CreateTransactionRequest request) {

        Transaction transaction = createTransactionUseCase.createDeposit(
                request.ownerId(),
                request.amount(),
                request.description()
        );

        return toResponse(transaction);
    }

    @PostMapping("/withdrawals")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createWithdrawal(
            @Valid @RequestBody CreateTransactionRequest request) {

        Transaction transaction = createTransactionUseCase.createWithdrawal(
                request.ownerId(),
                request.amount(),
                request.description()
        );

        return toResponse(transaction);
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransfer(
            @Valid @RequestBody CreateTransferRequest request) {

        Transaction transaction = createTransactionUseCase.createTransfer(
                request.ownerId(),
                request.destinationUserId(),
                request.amount(),
                request.description()
        );

        return toResponse(transaction);
    }

    @PostMapping("/purchases")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createPurchase(
            @Valid @RequestBody CreatePurchaseRequest request) {

        Transaction transaction = createTransactionUseCase.createPurchase(
                request.ownerId(),
                request.amount(),
                request.description(),
                request.category()
        );

        return toResponse(transaction);
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getOwnerId(),
                transaction.getDestinationUserId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getCreatedAt(),
                transaction.getDescription(),
                transaction.getCategory(),
                transaction.getStatus(),
                transaction.getRejectionReason()
        );
    }

    public record CreateTransactionRequest(
            @NotNull UUID ownerId,
            @NotNull @Positive BigDecimal amount,
            @NotBlank String description
    ) {}

    public record CreateTransferRequest(
            @NotNull UUID ownerId,
            @NotNull UUID destinationUserId,
            @NotNull @Positive BigDecimal amount,
            @NotBlank String description
    ) {}

    public record CreatePurchaseRequest(
            @NotNull UUID ownerId,
            @NotNull @Positive BigDecimal amount,
            @NotBlank String description,
            @NotBlank String category
    ) {}
}
