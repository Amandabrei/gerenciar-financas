package com.gerenciar.financas.transactions.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.UUID;

@Component
public class UserAuthenticationProvider implements AuthenticationProvider {

    private final RestClient restClient;

    public UserAuthenticationProvider(
            @Value("${services.user.base-url}") String userServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        try {
            UserAuthenticationResponse user = restClient.post()
                    .uri("/users/authenticate")
                    .body(new UserAuthenticationRequest(email, password))
                    .retrieve()
                    .body(UserAuthenticationResponse.class);

            if (user == null || user.id() == null) {
                throw new AuthenticationServiceException(
                        "Resposta inválida do serviço de usuários."
                );
            }

            AuthenticatedUser principal = new AuthenticatedUser(
                    user.id(),
                    user.name(),
                    user.email()
            );

            return UsernamePasswordAuthenticationToken.authenticated(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );

        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value()
                    || exception.getStatusCode().value() == HttpStatus.BAD_REQUEST.value()) {
                throw new BadCredentialsException("E-mail ou senha inválidos.");
            }

            throw new AuthenticationServiceException(
                    "Não foi possível validar as credenciais no serviço de usuários."
            );

        } catch (RestClientException exception) {
            throw new AuthenticationServiceException(
                    "Serviço de usuários indisponível para autenticação."
            );
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class
                .isAssignableFrom(authentication);
    }

    private record UserAuthenticationRequest(
            String email,
            String password
    ) {}

    private record UserAuthenticationResponse(
            UUID id,
            String name,
            String email
    ) {}
}
