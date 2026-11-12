package com.gerenciar.financas.user.application.port.in;

import com.gerenciar.financas.user.core.model.User;

public interface AuthenticateUserUseCase {

    User authenticate(String email, String password);
}
