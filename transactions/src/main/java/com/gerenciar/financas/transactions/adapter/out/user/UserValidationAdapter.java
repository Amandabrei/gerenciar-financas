package com.gerenciar.financas.transactions.adapter.out.user;

import com.gerenciar.financas.transactions.adapter.out.dto.UserResponse;
import com.gerenciar.financas.transactions.application.port.out.UserValidationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;

@Component
public class UserValidationAdapter implements UserValidationPort {

    private final RestClient restClient;

    public UserValidationAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${services.user.base-url}") String userServiceBaseUrl,
            @Value("${services.user.client-username}") String username,
            @Value("${services.user.client-password}") String password) {

        this.restClient = restClientBuilder
                .baseUrl(userServiceBaseUrl)
                .defaultHeaders(headers -> headers.setBasicAuth(username, password))
                .build();
    }

    @Override
    public boolean userExists(UUID userId) {
        try {
            UserResponse response = restClient.get()
                    .uri("/users/{id}", userId)
                    .retrieve()
                    .body(UserResponse.class);

            return response != null && userId.equals(response.getId());

        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                return false;
            }

            throw exception;
        }
    }
}
