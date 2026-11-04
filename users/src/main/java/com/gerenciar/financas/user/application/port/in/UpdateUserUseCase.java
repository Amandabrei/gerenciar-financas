package com.gerenciar.financas.user.application.port.in;

import com.gerenciar.financas.user.domain.model.User;
import java.util.UUID;

public interface UpdateUserUseCase {

    User updateUser(UUID id, String name, String email, String password);
}