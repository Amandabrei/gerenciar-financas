package com.gerenciar.financas.user.application.service;

import com.gerenciar.financas.user.application.port.in.UpdateUserUseCase;
import com.gerenciar.financas.user.application.port.out.UserRepositoryPort;
import com.gerenciar.financas.user.domain.exception.EmailAlreadyExistsException;
import com.gerenciar.financas.user.domain.exception.UserNotFoundException;
import com.gerenciar.financas.user.domain.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Locale;
import java.util.UUID;

@Service
public class UpdateUserService implements UpdateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public UpdateUserService(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoder passwordEncoder) {

        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User updateUser(UUID id, String name, String email, String password) {

        User existingUser = userRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuário não encontrado: " + id
                ));

        email = email.trim().toLowerCase(Locale.ROOT);

        userRepositoryPort.findByEmail(email)
                .filter(user -> !user.getId().equals(id))
                .ifPresent(user -> {
                    throw new EmailAlreadyExistsException(
                            "Já existe um usuário cadastrado com este e-mail"
                    );
                });

        String encodedPassword = password == null || password.isBlank()
                ? existingUser.getPassword()
                : passwordEncoder.encode(password);

        User updatedUser = new User(
                id,
                name,
                email,
                encodedPassword
        );

        return userRepositoryPort.save(updatedUser);
    }
}
