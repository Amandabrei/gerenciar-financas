package com.gerenciar.financas.transactions.adapter.in.controller;

import com.gerenciar.financas.transactions.adapter.in.dto.CreatePurchaseRequest;
import com.gerenciar.financas.transactions.adapter.in.dto.CreateTransactionRequest;
import com.gerenciar.financas.transactions.adapter.in.dto.CreateTransferRequest;
import com.gerenciar.financas.transactions.adapter.in.dto.TransactionResponse;
import com.gerenciar.financas.transactions.adapter.in.report.TransactionCsvReportGenerator;
import com.gerenciar.financas.transactions.application.model.ExpenseAnalytics;
import com.gerenciar.financas.transactions.application.port.in.CreateTransactionUseCase;
import com.gerenciar.financas.transactions.application.port.in.ExpenseAnalyticsUseCase;
import com.gerenciar.financas.transactions.application.port.in.FindTransactionUseCase;
import com.gerenciar.financas.transactions.domain.enums.TransactionType;
import com.gerenciar.financas.transactions.domain.exception.TransactionNotFoundException;
import com.gerenciar.financas.transactions.domain.model.Transaction;
import com.gerenciar.financas.transactions.infrastructure.security.AuthenticatedUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transações", description = "Gerenciamento de transações financeiras")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final FindTransactionUseCase findTransactionUseCase;
    private final ExpenseAnalyticsUseCase expenseAnalyticsUseCase;

    public TransactionController(
            CreateTransactionUseCase createTransactionUseCase,
            FindTransactionUseCase findTransactionUseCase,
            ExpenseAnalyticsUseCase expenseAnalyticsUseCase) {

        this.createTransactionUseCase = createTransactionUseCase;
        this.findTransactionUseCase = findTransactionUseCase;
        this.expenseAnalyticsUseCase = expenseAnalyticsUseCase;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar transação por ID",
            description = "Consulta uma transação pertencente ao usuário autenticado.")
    public ResponseEntity<TransactionResponse> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        Transaction transaction = findTransactionUseCase.findById(id)
                .filter(item -> item.getOwnerId().equals(authenticatedUser.id()))
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Transação não encontrada."
                ));

        return ResponseEntity.ok(toResponse(transaction));
    }

    @GetMapping
    @Operation(summary = "Listar transações",
            description = "Lista as transações do usuário autenticado com filtros opcionais.")
    public List<TransactionResponse> findAllByOwnerId(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate,
            @RequestParam(required = false) TransactionType type) {

        return findTransactionUseCase.findAllByOwnerId(
                        authenticatedUser.id(), startDate, endDate, type)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping(value = "/report", produces = "text/csv")
    @Operation(
            summary = "Exportar relatório de transações",
            description = "Exporta as transações do usuário autenticado em CSV, incluindo aprovadas e rejeitadas."
    )
    public ResponseEntity<byte[]> exportReport(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "A data inicial não pode ser posterior à data final."
            );
        }

        List<Transaction> transactions =
                findTransactionUseCase.findAllByOwnerId(
                        authenticatedUser.id(),
                        startDate.atStartOfDay(),
                        endDate.atTime(23, 59, 59, 999_999_999),
                        null
                );

        byte[] csv = TransactionCsvReportGenerator.generate(transactions);

        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"relatorio-transacoes.csv\""
                )
                .body(csv);
    }

    @PostMapping("/deposits")
    @Operation(summary = "Registrar depósito",
            description = "Registra um depósito na conta do usuário autenticado.")
    public ResponseEntity<TransactionResponse> createDeposit(
            @Valid @RequestBody CreateTransactionRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        Transaction transaction = createTransactionUseCase.createDeposit(
                authenticatedUser.id(),
                request.amount(),
                request.description()
        );

        return createdResponse(transaction);
    }

    @PostMapping("/withdrawal")
    @Operation(summary = "Registrar saque",
            description = "Registra um saque na conta do usuário autenticado.")
    public ResponseEntity<TransactionResponse> createWithdrawal(
            @Valid @RequestBody CreateTransactionRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        Transaction transaction = createTransactionUseCase.createWithdrawal(
                authenticatedUser.id(),
                request.amount(),
                request.description()
        );

        return createdResponse(transaction);
    }

    @PostMapping("/transfers")
    @Operation(summary = "Registrar transferência",
            description = "Registra uma transferência entre usuários.")
    public ResponseEntity<TransactionResponse> createTransfer(
            @Valid @RequestBody CreateTransferRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        Transaction transaction = createTransactionUseCase.createTransfer(
                authenticatedUser.id(),
                request.destinationUserId(),
                request.amount(),
                request.description()
        );

        return createdResponse(transaction);
    }

    @PostMapping("/purchase")
    @Operation(summary = "Registrar compra",
            description = "Registra uma compra com sua respectiva categoria.")
    public ResponseEntity<TransactionResponse> createPurchase(
            @Valid @RequestBody CreatePurchaseRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        Transaction transaction = createTransactionUseCase.createPurchase(
                authenticatedUser.id(),
                request.amount(),
                request.description(),
                request.category()
        );

        return createdResponse(transaction);
    }

    @GetMapping("/analytics/category")
    @Operation(
            summary = "Analisar despesas por categoria",
            description = "Retorna o total de cada categoria e o total geral."
    )
    public ExpenseAnalytics.CategorySummary analyzeByCategory(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return expenseAnalyticsUseCase.analyze(
                authenticatedUser.id(), startDate, endDate
        ).categorySummary();
    }

    @GetMapping("/analytics/day")
    @Operation(
            summary = "Analisar despesas por dia",
            description = "Retorna o total de despesas aprovadas em cada dia."
    )
    public List<ExpenseAnalytics.DailyExpense> analyzeByDay(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return expenseAnalyticsUseCase.analyze(
                authenticatedUser.id(), startDate, endDate
        ).dailyExpenses();
    }

    @GetMapping("/analytics/month")
    @Operation(
            summary = "Analisar despesas por mês",
            description = "Retorna o total de despesas aprovadas em cada mês."
    )
    public List<ExpenseAnalytics.MonthlyExpense> analyzeByMonth(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return expenseAnalyticsUseCase.analyze(
                authenticatedUser.id(), startDate, endDate
        ).monthlyExpenses();
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