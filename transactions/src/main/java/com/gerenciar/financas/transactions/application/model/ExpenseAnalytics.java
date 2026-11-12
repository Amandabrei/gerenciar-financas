package com.gerenciar.financas.transactions.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseAnalytics(

        CategorySummary categorySummary,

        List<DailyExpense> dailyExpenses,

        List<MonthlyExpense> monthlyExpenses

) {

    public record CategorySummary(
            List<CategoryExpense> categories,
            BigDecimal total
    ) {}

    public record CategoryExpense(
            String category,
            BigDecimal total
    ) {}

    public record DailyExpense(
            LocalDate date,
            BigDecimal total
    ) {}

    public record MonthlyExpense(
            String month,
            BigDecimal total
    ) {}
}