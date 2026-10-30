package com.gerenciar.financas.user.adapters.out.persistence.adapter;

import com.gerenciar.financas.user.application.port.out.UserRepositoryPort;
import com.gerenciar.financas.user.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserPersistenceAdapterTest {

    @Autowired
    private UserRepositoryPort userRepositoryPort;

    @Test
    void shouldSaveAndFindUser() {
        User user = new User(
                null,
                "Usuário Teste",
                "teste-" + UUID.randomUUID() + "@email.com",
                "senha-hash-teste"
        );

        // Act: salva o usuário no banco
        User savedUser = userRepositoryPort.save(user);

        // Assert: verifica se foi salvo corretamente
        assertNotNull(savedUser.getId());

        // Busca o usuário pelo ID gerado
        User foundUser = userRepositoryPort
                .findById(savedUser.getId())
                .orElseThrow();

        assertEquals(user.getName(), foundUser.getName());
        assertEquals(user.getEmail(), foundUser.getEmail());
    }
}
