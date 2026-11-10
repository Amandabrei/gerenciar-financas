package com.gerenciar.financas.user.infrastructure.security;

import com.gerenciar.financas.user.application.port.in.AuthenticateUserUseCase;
import com.gerenciar.financas.user.domain.exception.InvalidCredentialsException;
import com.gerenciar.financas.user.domain.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserAuthenticationProvider implements AuthenticationProvider {

    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final PasswordEncoder passwordEncoder;
    private final String serviceUsername;
    private final String encodedServicePassword;

    public UserAuthenticationProvider(
            AuthenticateUserUseCase authenticateUserUseCase,
            PasswordEncoder passwordEncoder,
            @Value("${security.service.username:}") String serviceUsername,
            @Value("${security.service.password:}") String servicePassword
    ) {
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.passwordEncoder = passwordEncoder;
        this.serviceUsername = serviceUsername;
        this.encodedServicePassword = servicePassword.isBlank()
                ? null
                : passwordEncoder.encode(servicePassword);
    }

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        if (isServiceAccount(username, password)) {
            AuthenticatedUser principal =
                    AuthenticatedUser.service(username);

            return UsernamePasswordAuthenticationToken.authenticated(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_SERVICE"))
            );
        }

        try {
            User user = authenticateUserUseCase.authenticate(
                    username,
                    password
            );

            AuthenticatedUser principal = AuthenticatedUser.user(
                    user.getId(),
                    user.getName(),
                    user.getEmail()
            );

            return UsernamePasswordAuthenticationToken.authenticated(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );

        } catch (InvalidCredentialsException exception) {
            throw new BadCredentialsException(
                    "E-mail ou senha inválidos."
            );
        }
    }

    private boolean isServiceAccount(String username, String password) {
        return serviceUsername != null
                && !serviceUsername.isBlank()
                && encodedServicePassword != null
                && serviceUsername.equals(username)
                && passwordEncoder.matches(password, encodedServicePassword);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class
                .isAssignableFrom(authentication);
    }
}
