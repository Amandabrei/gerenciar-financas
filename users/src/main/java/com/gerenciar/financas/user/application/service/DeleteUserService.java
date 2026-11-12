package com.gerenciar.financas.user.application.service;

import com.gerenciar.financas.user.application.port.in.DeleteUserUseCase;
import com.gerenciar.financas.user.application.port.out.UserRepositoryPort;
import com.gerenciar.financas.user.core.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteUserService implements DeleteUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public DeleteUserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public void deleteUser(UUID id) {
        userRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuário não encontrado: " + id
                ));

        userRepositoryPort.deleteById(id);
    }
}
