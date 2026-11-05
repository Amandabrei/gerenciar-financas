package com.gerenciar.financas.transactions.adapter.in.controller;

import com.gerenciar.financas.transactions.adapter.in.dto.CreatePurchaseRequest;
import com.gerenciar.financas.transactions.adapter.in.dto.CreateTransactionRequest;
import com.gerenciar.financas.transactions.adapter.in.dto.CreateTransferRequest;
import com.gerenciar.financas.transactions.adapter.in.dto.TransactionResponse;
import com.gerenciar.financas.transactions.application.port.in.CreateTransactionUseCase;
import com.gerenciar.financas.transactions.application.port.in.FindTransactionUseCase;
import com.gerenciar.financas.transactions.domain.enums.TransactionType;
import com.gerenciar.financas.transactions.domain.exception.TransactionNotFoundException;
import com.gerenciar.financas.transactions.domain.model.Transaction;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transações", description = "Gerenciamento de transações financeiras")
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
    @Operation(summary = "Consultar transação por ID", description = "Consulta uma transação pelo seu identificador.")
    public ResponseEntity<TransactionResponse> findById(@PathVariable UUID id) {
        Transaction transaction = findTransactionUseCase.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Transação não encontrada."
                ));

        return ResponseEntity.ok(toResponse(transaction));
    }

    @GetMapping
    @Operation(summary = "Listar transações", description = "Lista as transações de um usuário com filtros opcionais.")
    public List<TransactionResponse> findAllByOwnerId(
            @RequestParam UUID ownerId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate,
            @RequestParam(required = false) TransactionType type) {

        return findTransactionUseCase.findAllByOwnerId(
                        ownerId, startDate, endDate, type)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/deposits")
    @Operation(summary = "Registrar depósito", description = "Registra um depósito na conta do usuário.")
    public ResponseEntity<TransactionResponse> createDeposit(
            @Valid @RequestBody CreateTransactionRequest request) {

        Transaction transaction = createTransactionUseCase.createDeposit(
                request.ownerId(),
                request.amount(),
                request.description()
        );

        return createdResponse(transaction);
    }

    @PostMapping("/withdrawal")
    @Operation(summary = "Registrar saque", description = "Registra um saque na conta do usuário.")
    public ResponseEntity<TransactionResponse> createWithdrawal(
            @Valid @RequestBody CreateTransactionRequest request) {

        Transaction transaction = createTransactionUseCase.createWithdrawal(
                request.ownerId(),
                request.amount(),
                request.description()
        );

        return createdResponse(transaction);
    }

    @PostMapping("/transfers")
    @Operation(summary = "Registrar transferência", description = "Registra uma transferência entre usuários.")
    public ResponseEntity<TransactionResponse> createTransfer(
            @Valid @RequestBody CreateTransferRequest request) {

        Transaction transaction = createTransactionUseCase.createTransfer(
                request.ownerId(),
                request.destinationUserId(),
                request.amount(),
                request.description()
        );

        return createdResponse(transaction);
    }

    @PostMapping("/purchase")
    @Operation(summary = "Registrar compra", description = "Registra uma compra com sua respectiva categoria.")
    public ResponseEntity<TransactionResponse> createPurchase(
            @Valid @RequestBody CreatePurchaseRequest request) {

        Transaction transaction = createTransactionUseCase.createPurchase(
                request.ownerId(),
                request.amount(),
                request.description(),
                request.category()
        );

        return createdResponse(transaction);
    }

    private ResponseEntity<TransactionResponse> createdResponse(
            Transaction transaction) {

        var location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/transactions/{id}")
                .buildAndExpand(transaction.getId())
                .toUri();

        return ResponseEntity.created(location).body(toResponse(transaction));
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
                transaction.getRejectionReason(),
                transaction.getUsdExchangeRate(),
                transaction.getUsdQuoteDate(),
                transaction.getEurExchangeRate(),
                transaction.getEurQuoteDate()
        );
    }
}
