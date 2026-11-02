package com.gerenciar.financas.user.application.port.in;

import java.util.List;

public record ImportUsersCsvResult(
        int successCount,
        int failureCount,
        List<ImportUsersCsvFailure> failures
) {
}
