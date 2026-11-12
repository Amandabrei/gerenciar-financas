package com.gerenciar.financas.user.infrastructure.security;

import java.util.UUID;

public record AuthenticatedUser(
        UUID id,
        String name,
        String email,
        boolean serviceAccount
) {

    public static AuthenticatedUser user(
            UUID id,
            String name,
            String email
    ) {
        return new AuthenticatedUser(id, name, email, false);
    }

    public static AuthenticatedUser service(String username) {
        return new AuthenticatedUser(null, "Internal Service", username, true);
    }
}
