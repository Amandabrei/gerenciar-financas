package com.gerenciar.financas.user.application.port.in;

import com.gerenciar.financas.user.domain.model.User;

public interface CreateUserUseCase {

    User createUser(String name, String email, String password);
}
