package com.gerenciar.financas.user.application.service;

import com.gerenciar.financas.user.application.port.in.CreateUserUseCase;
import com.gerenciar.financas.user.application.port.out.UserRepositoryPort;
import com.gerenciar.financas.user.core.exception.EmailAlreadyExistsException;
import com.gerenciar.financas.user.core.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CreateUserService implements CreateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public CreateUserService(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoder passwordEncoder) {

        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(String name, String email, String password) {

        email = email.trim().toLowerCase(Locale.ROOT);

        userRepositoryPort.findByEmail(email)
                .ifPresent(user -> {
                    throw new EmailAlreadyExistsException(
                            "Já existe um usuário cadastrado com este e-mail"
                    );
                });

        String encodedPassword = passwordEncoder.encode(password);

        User user = new User(
                null,
                name,
                email,
                encodedPassword
        );

        return userRepositoryPort.save(user);
    }
}
