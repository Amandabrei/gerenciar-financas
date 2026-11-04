package com.gerenciar.financas.user.application.service;

import com.gerenciar.financas.user.application.port.in.FindAllUsersUseCase;
import com.gerenciar.financas.user.application.port.out.UserRepositoryPort;
import com.gerenciar.financas.user.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindAllUserService implements FindAllUsersUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public FindAllUserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public List<User> findAllUsers() {
        return userRepositoryPort.findAll();
    }
}
