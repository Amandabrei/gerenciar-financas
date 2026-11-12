package com.gerenciar.financas.transactions.infrastructure.security;

import java.util.UUID;

public record AuthenticatedUser(
        UUID id,
        String name,
        String email
) {}