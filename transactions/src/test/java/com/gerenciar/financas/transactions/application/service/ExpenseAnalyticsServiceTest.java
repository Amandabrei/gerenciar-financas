package com.gerenciar.financas.transactions.application.service;

import com.gerenciar.financas.transactions.application.model.ExpenseAnalytics;
import com.gerenciar.financas.transactions.application.port.in.FindTransactionUseCase;
import com.gerenciar.financas.transactions.domain.enums.TransactionStatus;
import com.gerenciar.financas.transactions.domain.enums.TransactionType;
import com.gerenciar.financas.transactions.domain.model.Transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpenseAnalyticsServiceTest {

    private FindTransactionUseCase findTransactionUseCase;
    private ExpenseAnalyticsService service;

    @BeforeEach
    void setUp() {
        findTransactionUseCase = mock(FindTransactionUseCase.class);
        service = new ExpenseAnalyticsService(findTransactionUseCase);
    }

    @Test
    void shouldCalculateApprovedExpensesByCategoryDayAndMonth() {
        UUID ownerId = UUID.randomUUID();

        LocalDateTime firstDay = LocalDateTime.of(2026, 11, 2, 10, 0);
        LocalDateTime secondDay = LocalDateTime.of(2026, 11, 3, 15, 30);

        List<Transaction> transactions = List.of(
                transaction(ownerId, TransactionType.WITHDRAWAL,
                        "100.00", firstDay, "ALIMENTACAO",
                        TransactionStatus.APPROVED, null),

                transaction(ownerId, TransactionType.TRANSFER,
                        "50.00", firstDay, "ALIMENTACAO",
                        TransactionStatus.APPROVED, null),

                transaction(ownerId, TransactionType.PURCHASE,
                        "200.00", secondDay, "MORADIA",
                        TransactionStatus.APPROVED, null),

                transaction(ownerId, TransactionType.DEPOSIT,
                        "900.00", secondDay, null,
                        TransactionStatus.APPROVED, null),

                transaction(ownerId, TransactionType.PURCHASE,
                        "800.00", secondDay, "ELETRONICOS",
                        TransactionStatus.REJECTED, "Compra rejeitada")
        );

        when(findTransactionUseCase.findAllByOwnerId(
                eq(ownerId), any(), any(), isNull()
        )).thenReturn(transactions);

        ExpenseAnalytics result = service.analyze(
                ownerId,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30)
        );

        assertEquals(new BigDecimal("350.00"),
                result.categorySummary().total());

        assertEquals(2, result.categorySummary().categories().size());

        assertEquals("ALIMENTACAO",
                result.categorySummary().categories().get(0).category());
        assertEquals(new BigDecimal("150.00"),
                result.categorySummary().categories().get(0).total());

        assertEquals("MORADIA",
                result.categorySummary().categories().get(1).category());
        assertEquals(new BigDecimal("200.00"),
                result.categorySummary().categories().get(1).total());

        assertEquals(2, result.dailyExpenses().size());
        assertEquals(new BigDecimal("150.00"),
                result.dailyExpenses().get(0).total());
        assertEquals(new BigDecimal("200.00"),
                result.dailyExpenses().get(1).total());

        assertEquals(1, result.monthlyExpenses().size());
        assertEquals("2026-11",
                result.monthlyExpenses().get(0).month());
        assertEquals(new BigDecimal("350.00"),
                result.monthlyExpenses().get(0).total());
    }

    @Test
    void shouldUseCurrentMonthWhenDatesAreNotProvided() {
        UUID ownerId = UUID.randomUUID();

        when(findTransactionUseCase.findAllByOwnerId(
                eq(ownerId), any(), any(), isNull()
        )).thenReturn(List.of());

        service.analyze(ownerId, null, null);

        YearMonth currentMonth = YearMonth.now();

        verify(findTransactionUseCase).findAllByOwnerId(
                ownerId,
                currentMonth.atDay(1).atStartOfDay(),
                currentMonth.atEndOfMonth().atTime(23, 59, 59, 999_999_999),
                null
        );
    }

    @Test
    void shouldRejectPeriodWhenOnlyStartDateIsProvided() {
        UUID ownerId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> service.analyze(
                        ownerId, LocalDate.of(2026, 11, 1), null));

        verifyNoInteractions(findTransactionUseCase);
    }

    @Test
    void shouldRejectPeriodWhenOnlyEndDateIsProvided() {
        UUID ownerId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> service.analyze(
                        ownerId, null, LocalDate.of(2026, 11, 30)));

        verifyNoInteractions(findTransactionUseCase);
    }

    @Test
    void shouldRejectPeriodWhenStartDateIsAfterEndDate() {
        UUID ownerId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> service.analyze(
                        ownerId,
                        LocalDate.of(2026, 11, 30),
                        LocalDate.of(2026, 11, 1)));

        verifyNoInteractions(findTransactionUseCase);
    }

    private Transaction transaction(UUID ownerId, TransactionType type,
                                    String amount, LocalDateTime createdAt, String category,
                                    TransactionStatus status, String rejectionReason) {

        UUID destinationUserId = type == TransactionType.TRANSFER
                ? UUID.randomUUID()
                : null;

        return new Transaction(
                UUID.randomUUID(),
                ownerId,
                destinationUserId,
                type,
                new BigDecimal(amount),
                createdAt,
                "Transação de teste",
                category,
                status,
                rejectionReason,
                null, null, null, null, null
        );
    }
}
