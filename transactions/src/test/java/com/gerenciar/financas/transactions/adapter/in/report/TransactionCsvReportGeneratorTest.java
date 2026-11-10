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

        String csv = generate(approved, rejected);

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

        String csv = generate(transaction);

        assertTrue(csv.contains("\"Compra de \"\"arroz; feijão\"\"\""));
    }

    @Test
    void deveRepresentarCamposNulosComoVazios() {
        Transaction transaction = transaction(
                TransactionStatus.APPROVED, null, null
        );

        String csv = generate(transaction);

        assertTrue(csv.contains("\"\""));
    }

    @Test
    void deveFormatarDataEValorParaExcelBrasileiro() {
        Transaction transaction = transaction(
                TransactionStatus.APPROVED,
                null,
                "Compra mercado"
        );

        String csv = generate(transaction);

        assertTrue(csv.contains("\"10/11/2026 14:30:00\""));
        assertTrue(csv.contains("\"125,50\""));
    }

    @Test
    void deveNeutralizarFormulaEmCategoriaDescricaoEMotivo() {
        Transaction transaction = transaction(
                TransactionStatus.REJECTED,
                "@SOMA(A1:A2)",
                "=HYPERLINK(\"https://exemplo.com\")",
                "+CATEGORIA"
        );

        String csv = generate(transaction);

        assertTrue(csv.contains("\"'+CATEGORIA\""));
        assertTrue(csv.contains("\"'=HYPERLINK(\"\"https://exemplo.com\"\")\""));
        assertTrue(csv.contains("\"'@SOMA(A1:A2)\""));
    }

    @Test
    void deveNeutralizarTodosOsCaracteresDeFormula() {
        List<Transaction> transactions = List.of(
                transaction(TransactionStatus.APPROVED, null, "=formula"),
                transaction(TransactionStatus.APPROVED, null, "+formula"),
                transaction(TransactionStatus.APPROVED, null, "-formula"),
                transaction(TransactionStatus.APPROVED, null, "@formula")
        );

        String csv = new String(
                TransactionCsvReportGenerator.generate(transactions),
                StandardCharsets.UTF_8
        );

        assertTrue(csv.contains("\"'=formula\""));
        assertTrue(csv.contains("\"'+formula\""));
        assertTrue(csv.contains("\"'-formula\""));
        assertTrue(csv.contains("\"'@formula\""));
    }

    private String generate(Transaction... transactions) {
        return new String(
                TransactionCsvReportGenerator.generate(List.of(transactions)),
                StandardCharsets.UTF_8
        );
    }

    private Transaction transaction(
            TransactionStatus status,
            String rejectionReason,
            String description
    ) {
        return transaction(
                status, rejectionReason, description, "ALIMENTACAO"
        );
    }

    private Transaction transaction(
            TransactionStatus status,
            String rejectionReason,
            String description,
            String category
    ) {
        return new Transaction(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                TransactionType.PURCHASE,
                new BigDecimal("125.50"),
                LocalDateTime.of(2026, 11, 10, 14, 30),
                description,
                category,
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
