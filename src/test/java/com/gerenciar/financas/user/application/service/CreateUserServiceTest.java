package com.gerenciar.financas.user.application.service;

import com.gerenciar.financas.user.application.port.out.UserRepositoryPort;
import com.gerenciar.financas.user.domain.exception.EmailAlreadyExistsException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateUserService createUserService;

    @Test
    void deveCriarUsuarioComSenhaCriptografada() {

        when(userRepositoryPort.findByEmail("teste@email.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("Senha123456"))
                .thenReturn("senha-criptografada");

        when(userRepositoryPort.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User user = createUserService.createUser(
                "Usuário Teste",
                "teste@email.com",
                "Senha123456"
        );

        assertEquals("Usuário Teste", user.getName());
        assertEquals("teste@email.com", user.getEmail());
        assertEquals("senha-criptografada", user.getPassword());

        verify(userRepositoryPort).save(any(User.class));
    }

    @Test
    void deveImpedirCadastroComEmailDuplicado() {

        User existingUser = new User(
                UUID.randomUUID(),
                "Usuário Existente",
                "teste@email.com",
                "senha-criptografada"
        );

        when(userRepositoryPort.findByEmail("teste@email.com"))
                .thenReturn(Optional.of(existingUser));

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> createUserService.createUser(
                        "Novo Usuário",
                        "teste@email.com",
                        "Senha123456"
                )
        );

        verify(userRepositoryPort, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void deveNormalizarEmailAntesDeConsultarESalvar() {

        when(userRepositoryPort.findByEmail("ana@email.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("Senha123456"))
                .thenReturn("senha-criptografada");

        when(userRepositoryPort.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User user = createUserService.createUser(
                "Ana",
                " Ana@Email.com ",
                "Senha123456"
        );

        assertEquals("ana@email.com", user.getEmail());

        verify(userRepositoryPort).findByEmail("ana@email.com");
        verify(userRepositoryPort).save(any(User.class));
    }
}
