package com.gerenciar.financas.user.application.service;

import com.gerenciar.financas.user.application.port.out.UserRepositoryPort;
import com.gerenciar.financas.user.core.exception.UserNotFoundException;
import com.gerenciar.financas.user.core.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteUserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private DeleteUserService deleteUserService;

    @Test
    void deveExcluirUsuarioQuandoEncontrado() {
        UUID id = UUID.randomUUID();

        User user = new User(
                id,
                "Usuário Teste",
                "teste@email.com",
                "senha-criptografada"
        );

        when(userRepositoryPort.findById(id))
                .thenReturn(Optional.of(user));

        deleteUserService.deleteUser(id);

        verify(userRepositoryPort).findById(id);
        verify(userRepositoryPort).deleteById(id);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {
        UUID id = UUID.randomUUID();

        when(userRepositoryPort.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> deleteUserService.deleteUser(id)
        );

        verify(userRepositoryPort, never()).deleteById(any(UUID.class));
    }
}
