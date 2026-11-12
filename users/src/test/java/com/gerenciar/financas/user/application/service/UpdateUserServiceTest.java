package com.gerenciar.financas.user.application.service;

import com.gerenciar.financas.user.application.port.out.UserRepositoryPort;
import com.gerenciar.financas.user.core.exception.EmailAlreadyExistsException;
import com.gerenciar.financas.user.core.exception.UserNotFoundException;
import com.gerenciar.financas.user.core.model.User;
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
class UpdateUserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UpdateUserService updateUserService;

    @Test
    void deveAtualizarUsuarioComSucesso() {
        UUID id = UUID.randomUUID();

        User existingUser = new User(
                id, "Nome Antigo", "antigo@email.com", "senha-antiga"
        );

        when(userRepositoryPort.findById(id))
                .thenReturn(Optional.of(existingUser));

        when(userRepositoryPort.findByEmail("novo@email.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("NovaSenha123"))
                .thenReturn("senha-criptografada");

        when(userRepositoryPort.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = updateUserService.updateUser(
                id, "Nome Novo", "novo@email.com", "NovaSenha123"
        );

        assertEquals(id, result.getId());
        assertEquals("Nome Novo", result.getName());
        assertEquals("novo@email.com", result.getEmail());
        assertEquals("senha-criptografada", result.getPassword());

        verify(userRepositoryPort).save(any(User.class));
    }

    @Test
    void deveImpedirAtualizacaoComEmailDeOutroUsuario() {
        UUID id = UUID.randomUUID();

        User existingUser = new User(
                id, "Usuário Atual", "atual@email.com", "senha"
        );

        User otherUser = new User(
                UUID.randomUUID(), "Outro Usuário", "outro@email.com", "senha"
        );

        when(userRepositoryPort.findById(id))
                .thenReturn(Optional.of(existingUser));

        when(userRepositoryPort.findByEmail("outro@email.com"))
                .thenReturn(Optional.of(otherUser));

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> updateUserService.updateUser(
                        id, "Nome Novo", "outro@email.com", "NovaSenha123"
                )
        );

        verify(userRepositoryPort, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {
        UUID id = UUID.randomUUID();

        when(userRepositoryPort.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> updateUserService.updateUser(
                        id, "Nome", "teste@email.com", "Senha123456"
                )
        );

        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    void deveNormalizarEmailAntesDeConsultarESalvar() {
        UUID id = UUID.randomUUID();
        User existingUser = new User(id, "Ana", "ana@email.com", "senha-criptografada");

        when(userRepositoryPort.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepositoryPort.findByEmail("ana@email.com")).thenReturn(Optional.empty());
        when(userRepositoryPort.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = updateUserService.updateUser(
                id, "Ana", " Ana@Email.com ", null
        );

        assertEquals("ana@email.com", updatedUser.getEmail());
        verify(userRepositoryPort).findByEmail("ana@email.com");
        verify(userRepositoryPort).save(any(User.class));
    }

    @Test
    void deveManterSenhaAtualQuandoNovaSenhaNaoForInformada() {
        UUID id = UUID.randomUUID();
        String senhaAtual = "senha-criptografada";
        User existingUser = new User(id, "Ana", "ana@email.com", senhaAtual);

        when(userRepositoryPort.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepositoryPort.findByEmail("ana@email.com")).thenReturn(Optional.empty());
        when(userRepositoryPort.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = updateUserService.updateUser(
                id, "Ana", "ana@email.com", null
        );

        assertEquals(senhaAtual, updatedUser.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepositoryPort).save(any(User.class));
    }
}