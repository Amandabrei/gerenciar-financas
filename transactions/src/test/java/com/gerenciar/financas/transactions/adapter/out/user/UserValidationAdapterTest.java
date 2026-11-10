package com.gerenciar.financas.transactions.adapter.out.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UserValidationAdapterTest {

    private UserValidationAdapter adapter;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();

        server = MockRestServiceServer.bindTo(builder).build();

        adapter = new UserValidationAdapter(
                builder,
                "http://localhost:8082",
                "ms2-service-test",
                "SenhaServicoTeste123!"
        );
    }

    @Test
    void shouldReturnTrueWhenUserExists() {
        UUID userId = UUID.randomUUID();

        server.expect(requestTo(
                        "http://localhost:8082/users/" + userId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                            "id": "%s",
                            "name": "Amanda",
                            "email": "amanda@email.com"
                        }
                        """.formatted(userId),
                        MediaType.APPLICATION_JSON
                ));

        assertTrue(adapter.userExists(userId));
        server.verify();
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        server.expect(requestTo(
                        "http://localhost:8082/users/" + userId))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertFalse(adapter.userExists(userId));
        server.verify();
    }

    @Test
    void shouldPropagateExceptionWhenUserServiceFails() {
        UUID userId = UUID.randomUUID();

        server.expect(requestTo(
                        "http://localhost:8082/users/" + userId))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(
                RuntimeException.class,
                () -> adapter.userExists(userId)
        );

        server.verify();
    }
}