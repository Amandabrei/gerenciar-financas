package com.gerenciar.financas.transactions.application.port.in;

import com.gerenciar.financas.transactions.application.model.ExpenseAnalytics;

import java.time.LocalDate;
import java.util.UUID;

public interface ExpenseAnalyticsUseCase {

    ExpenseAnalytics analyze(
            UUID ownerId,
            LocalDate startDate,
            LocalDate endDate
    );
}
