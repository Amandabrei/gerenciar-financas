package com.gerenciar.financas.transactions.adapter.in.report;

import com.gerenciar.financas.transactions.domain.model.Transaction;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class TransactionCsvReportGenerator {

    private static final String HEADER =
            "data;tipo;categoria;descricao;valor_brl;status;motivo_rejeicao";

    private TransactionCsvReportGenerator() {
    }

    public static byte[] generate(List<Transaction> transactions) {
        StringBuilder csv = new StringBuilder();

        // BOM UTF-8 para facilitar a abertura correta no Excel.
        csv.append('\uFEFF');
        csv.append(HEADER).append("\r\n");

        for (Transaction transaction : transactions) {
            csv.append(escape(transaction.getCreatedAt().toString())).append(';')
                    .append(escape(transaction.getType().name())).append(';')
                    .append(escape(transaction.getCategory())).append(';')
                    .append(escape(transaction.getDescription())).append(';')
                    .append(escape(transaction.getAmount().toPlainString())).append(';')
                    .append(escape(transaction.getStatus().name())).append(';')
                    .append(escape(transaction.getRejectionReason()))
                    .append("\r\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        if (value == null) {
            return "\"\"";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
