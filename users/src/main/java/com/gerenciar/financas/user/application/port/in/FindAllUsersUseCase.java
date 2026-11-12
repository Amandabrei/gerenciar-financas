package com.gerenciar.financas.user.application.port.in;

import com.gerenciar.financas.user.core.model.User;

import java.util.List;

public interface FindAllUsersUseCase {

    List<User> findAllUsers();
}
