package com.gerenciar.financas.user.application.service;

import com.gerenciar.financas.user.application.port.out.UserRepositoryPort;
import com.gerenciar.financas.user.core.model.User;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindAllUsersServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private FindAllUserService findAllUsersService;

    @Test
    void deveRetornarTodosOsUsuarios() {
        User user1 = new User(
                UUID.randomUUID(),
                "Usuário 1",
                "usuario1@email.com",
                "senha-1"
        );

        User user2 = new User(
                UUID.randomUUID(),
                "Usuário 2",
                "usuario2@email.com",
                "senha-2"
        );

        when(userRepositoryPort.findAll())
                .thenReturn(List.of(user1, user2));

        List<User> result = findAllUsersService.findAllUsers();

        assertEquals(2, result.size());
        assertEquals("Usuário 1", result.get(0).getName());
        assertEquals("Usuário 2", result.get(1).getName());

        verify(userRepositoryPort).findAll();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremUsuarios() {
        when(userRepositoryPort.findAll())
                .thenReturn(List.of());

        List<User> result = findAllUsersService.findAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepositoryPort).findAll();
    }
}
