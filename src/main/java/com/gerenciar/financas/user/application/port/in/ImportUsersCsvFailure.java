package com.gerenciar.financas.user.application.port.in;

public record ImportUsersCsvFailure(
        int lineNumber,
        String reason
) {
}
