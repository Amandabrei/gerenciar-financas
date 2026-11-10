package com.gerenciar.financas.transactions.adapter.in.report;

import com.gerenciar.financas.transactions.domain.model.Transaction;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class TransactionCsvReportGenerator {

    private static final String HEADER =
            "data;tipo;categoria;descricao;valor_brl;status;motivo_rejeicao";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private TransactionCsvReportGenerator() {
    }

    public static byte[] generate(List<Transaction> transactions) {
        StringBuilder csv = new StringBuilder();

        // BOM UTF-8 para facilitar a abertura correta no Excel.
        csv.append('\uFEFF');
        csv.append(HEADER).append("\r\n");

        for (Transaction transaction : transactions) {
            String formattedDate =
                    transaction.getCreatedAt().format(DATE_FORMATTER);

            String formattedAmount =
                    transaction.getAmount().toPlainString().replace('.', ',');

            csv.append(escape(formattedDate, false)).append(';')
                    .append(escape(transaction.getType().name(), false)).append(';')
                    .append(escape(transaction.getCategory(), true)).append(';')
                    .append(escape(transaction.getDescription(), true)).append(';')
                    .append(escape(formattedAmount, false)).append(';')
                    .append(escape(transaction.getStatus().name(), false)).append(';')
                    .append(escape(transaction.getRejectionReason(), true))
                    .append("\r\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String escape(String value, boolean protectFormula) {
        if (value == null) {
            return "\"\"";
        }

        String safeValue = value;

        if (protectFormula && startsWithFormulaCharacter(safeValue)) {
            safeValue = "'" + safeValue;
        }

        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }

    private static boolean startsWithFormulaCharacter(String value) {
        if (value.isEmpty()) {
            return false;
        }

        char firstCharacter = value.charAt(0);

        return firstCharacter == '='
                || firstCharacter == '+'
                || firstCharacter == '-'
                || firstCharacter == '@';
    }
}
