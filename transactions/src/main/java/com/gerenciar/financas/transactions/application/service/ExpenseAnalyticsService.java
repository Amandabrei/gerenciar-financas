package com.gerenciar.financas.transactions.application.service;

import com.gerenciar.financas.transactions.application.model.ExpenseAnalytics;
import com.gerenciar.financas.transactions.application.port.in.ExpenseAnalyticsUseCase;
import com.gerenciar.financas.transactions.application.port.in.FindTransactionUseCase;
import com.gerenciar.financas.transactions.domain.enums.TransactionStatus;
import com.gerenciar.financas.transactions.domain.enums.TransactionType;
import com.gerenciar.financas.transactions.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class ExpenseAnalyticsService implements ExpenseAnalyticsUseCase {

    private static final String DEFAULT_CATEGORY = "SEM_CATEGORIA";

    private final FindTransactionUseCase findTransactionUseCase;

    public ExpenseAnalyticsService(
            FindTransactionUseCase findTransactionUseCase) {
        this.findTransactionUseCase = findTransactionUseCase;
    }

    @Override
    public ExpenseAnalytics analyze(
            UUID ownerId,
            LocalDate startDate,
            LocalDate endDate) {

        LocalDate[] period = resolvePeriod(startDate, endDate);

        List<Transaction> expenses = findTransactionUseCase.findAllByOwnerId(
                        ownerId,
                        period[0].atStartOfDay(),
                        period[1].atTime(23, 59, 59, 999_999_999),
                        null
                )
                .stream()
                .filter(this::isApprovedExpense)
                .toList();

        return new ExpenseAnalytics(
                summarizeByCategory(expenses),
                summarizeByDay(expenses),
                summarizeByMonth(expenses)
        );
    }

    private LocalDate[] resolvePeriod(
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate == null && endDate == null) {
            YearMonth currentMonth = YearMonth.now();

            return new LocalDate[]{
                    currentMonth.atDay(1),
                    currentMonth.atEndOfMonth()
            };
        }

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Informe as duas datas: startDate e endDate."
            );
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "A data inicial não pode ser posterior à data final."
            );
        }

        return new LocalDate[]{startDate, endDate};
    }

    private boolean isApprovedExpense(Transaction transaction) {
        return transaction.getStatus() == TransactionStatus.APPROVED
                && (transaction.getType() == TransactionType.WITHDRAWAL
                || transaction.getType() == TransactionType.TRANSFER
                || transaction.getType() == TransactionType.PURCHASE);
    }

    private ExpenseAnalytics.CategorySummary summarizeByCategory(
            List<Transaction> expenses) {

        Map<String, BigDecimal> totals = new TreeMap<>();

        for (Transaction transaction : expenses) {
            String category = transaction.getCategory();

            if (category == null || category.isBlank()) {
                category = DEFAULT_CATEGORY;
            }

            totals.merge(category, transaction.getAmount(), BigDecimal::add);
        }

        List<ExpenseAnalytics.CategoryExpense> categories = totals.entrySet()
                .stream()
                .map(entry -> new ExpenseAnalytics.CategoryExpense(
                        entry.getKey(),
                        entry.getValue()
                ))
                .toList();

        BigDecimal total = expenses.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ExpenseAnalytics.CategorySummary(categories, total);
    }

    private List<ExpenseAnalytics.DailyExpense> summarizeByDay(
            List<Transaction> expenses) {

        Map<LocalDate, BigDecimal> totals = new TreeMap<>();

        for (Transaction transaction : expenses) {
            LocalDate date = transaction.getCreatedAt().toLocalDate();

            totals.merge(date, transaction.getAmount(), BigDecimal::add);
        }

        return totals.entrySet()
                .stream()
                .map(entry -> new ExpenseAnalytics.DailyExpense(
                        entry.getKey(),
                        entry.getValue()
                ))
                .toList();
    }

    private List<ExpenseAnalytics.MonthlyExpense> summarizeByMonth(
            List<Transaction> expenses) {

        Map<YearMonth, BigDecimal> totals = new TreeMap<>();

        for (Transaction transaction : expenses) {
            YearMonth month = YearMonth.from(transaction.getCreatedAt());

            totals.merge(month, transaction.getAmount(), BigDecimal::add);
        }

        return totals.entrySet()
                .stream()
                .map(entry -> new ExpenseAnalytics.MonthlyExpense(
                        entry.getKey().toString(),
                        entry.getValue()
                ))
                .toList();
    }
}
