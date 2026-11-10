package com.gerenciar.financas.user.application.port.in;

import com.gerenciar.financas.user.domain.model.User;

public interface AuthenticateUserUseCase {

    User authenticate(String email, String password);
}
