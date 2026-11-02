package com.gerenciar.financas.user.application.service;

import com.gerenciar.financas.user.application.port.in.FindUserByIdUseCase;
import com.gerenciar.financas.user.application.port.out.UserRepositoryPort;
import com.gerenciar.financas.user.domain.exception.UserNotFoundException;
import com.gerenciar.financas.user.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FindUserByIdService implements FindUserByIdUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public FindUserByIdService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public User findUserById(UUID id) {
        return userRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuário não encontrado: " + id
                ));
    }
}
