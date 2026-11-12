package com.gerenciar.financas.user.application.port.in;

import com.gerenciar.financas.user.core.model.User;
import java.util.UUID;

public interface FindUserByIdUseCase {

    User findUserById(UUID id);
}