package com.gerenciar.financas.transactions.application.port.out;

import java.util.UUID;

public interface UserValidationPort {

    boolean userExists(UUID userId);
}
