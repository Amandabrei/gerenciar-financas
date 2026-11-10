package com.gerenciar.financas.user.application.service;

import com.gerenciar.financas.user.application.port.out.UserRepositoryPort;
import com.gerenciar.financas.user.domain.exception.InvalidCredentialsException;
import com.gerenciar.financas.user.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticateUserService authenticateUserService;

    @Test
    void deveAutenticarUsuarioComCredenciaisValidas() {

        User user = new User(
                UUID.randomUUID(),
                "Ana",
                "ana@email.com",
                "senha-criptografada"
        );

        when(userRepositoryPort.findByEmail("ana@email.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Senha123456",
                "senha-criptografada"
        )).thenReturn(true);

        User authenticated = authenticateUserService.authenticate(
                "ana@email.com",
                "Senha123456"
        );

        assertEquals(user.getId(), authenticated.getId());
        assertEquals(user.getEmail(), authenticated.getEmail());

        verify(passwordEncoder).matches(
                "Senha123456",
                "senha-criptografada"
        );
    }

    @Test
    void deveNormalizarEmailAntesDeBuscarUsuario() {

        User user = new User(
                UUID.randomUUID(),
                "Ana",
                "ana@email.com",
                "senha-criptografada"
        );

        when(userRepositoryPort.findByEmail("ana@email.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Senha123456",
                "senha-criptografada"
        )).thenReturn(true);

        authenticateUserService.authenticate(
                " Ana@Email.com ",
                "Senha123456"
        );

        verify(userRepositoryPort).findByEmail("ana@email.com");
    }

    @Test
    void deveRejeitarEmailInexistente() {

        when(userRepositoryPort.findByEmail("inexistente@email.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> authenticateUserService.authenticate(
                        "inexistente@email.com",
                        "Senha123456"
                )
        );

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void deveRejeitarSenhaIncorreta() {

        User user = new User(
                UUID.randomUUID(),
                "Ana",
                "ana@email.com",
                "senha-criptografada"
        );

        when(userRepositoryPort.findByEmail("ana@email.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "SenhaErrada",
                "senha-criptografada"
        )).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authenticateUserService.authenticate(
                        "ana@email.com",
                        "SenhaErrada"
                )
        );
    }
}
