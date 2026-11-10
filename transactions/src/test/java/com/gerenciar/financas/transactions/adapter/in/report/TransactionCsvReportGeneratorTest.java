package com.gerenciar.financas.transactions.adapter.in.report;

import com.gerenciar.financas.transactions.domain.enums.TransactionStatus;
import com.gerenciar.financas.transactions.domain.enums.TransactionType;
import com.gerenciar.financas.transactions.domain.model.Transaction;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionCsvReportGeneratorTest {

    @Test
    void deveGerarCsvComCabecalhoEBomUtf8() {
        byte[] result =
                TransactionCsvReportGenerator.generate(List.of());

        String csv = new String(result, StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("\uFEFF"));
        assertTrue(csv.contains(
                "data;tipo;categoria;descricao;valor_brl;status;motivo_rejeicao"
        ));
    }

    @Test
    void deveIncluirTransacoesAprovadasERejeitadas() {
        Transaction approved = transaction(
                TransactionStatus.APPROVED, null, "Compra mercado"
        );

        Transaction rejected = transaction(
                TransactionStatus.REJECTED,
                "Saldo insuficiente",
                "Compra recusada"
        );

        String csv = new String(
                TransactionCsvReportGenerator.generate(
                        List.of(approved, rejected)
                ),
                StandardCharsets.UTF_8
        );

        assertTrue(csv.contains("APPROVED"));
        assertTrue(csv.contains("REJECTED"));
        assertTrue(csv.contains("Saldo insuficiente"));
        assertTrue(csv.contains("Compra mercado"));
        assertTrue(csv.contains("Compra recusada"));
    }

    @Test
    void deveEscaparAspasEPontoEVirgula() {
        Transaction transaction = transaction(
                TransactionStatus.APPROVED,
                null,
                "Compra de \"arroz; feijão\""
        );

        String csv = new String(
                TransactionCsvReportGenerator.generate(List.of(transaction)),
                StandardCharsets.UTF_8
        );

        assertTrue(csv.contains("\"Compra de \"\"arroz; feijão\"\"\""));
    }

    @Test
    void deveRepresentarCamposNulosComoVazios() {
        Transaction transaction = transaction(
                TransactionStatus.APPROVED, null, null
        );

        String csv = new String(
                TransactionCsvReportGenerator.generate(List.of(transaction)),
                StandardCharsets.UTF_8
        );

        assertTrue(csv.contains("\"\""));
    }

    private Transaction transaction(
            TransactionStatus status,
            String rejectionReason,
            String description
    ) {
        return new Transaction(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                TransactionType.PURCHASE,
                new BigDecimal("125.50"),
                LocalDateTime.of(2026, 11, 10, 14, 30),
                description,
                "ALIMENTACAO",
                status,
                rejectionReason,
                null,
                null,
                null,
                null,
                null
        );
    }
}