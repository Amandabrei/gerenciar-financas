package com.gerenciar.financas.user.application.service;

import com.gerenciar.financas.user.application.port.out.UserRepositoryPort;
import com.gerenciar.financas.user.domain.exception.UserNotFoundException;
import com.gerenciar.financas.user.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindUserByIdServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private FindUserByIdService findUserByIdService;

    @Test
    void deveRetornarUsuarioQuandoEncontrado() {
        UUID id = UUID.randomUUID();

        User user = new User(
                id,
                "Usuário Teste",
                "teste@email.com",
                "senha-criptografada"
        );

        when(userRepositoryPort.findById(id))
                .thenReturn(Optional.of(user));

        User result = findUserByIdService.findUserById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Usuário Teste", result.getName());
        assertEquals("teste@email.com", result.getEmail());

        verify(userRepositoryPort).findById(id);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        UUID id = UUID.randomUUID();

        when(userRepositoryPort.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> findUserByIdService.findUserById(id)
        );

        verify(userRepositoryPort).findById(id);
    }
}
