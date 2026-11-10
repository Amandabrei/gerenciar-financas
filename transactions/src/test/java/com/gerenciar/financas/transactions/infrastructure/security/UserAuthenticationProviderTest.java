package com.gerenciar.financas.transactions.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UserAuthenticationProviderTest {

    private UserAuthenticationProvider provider;
    private MockRestServiceServer server;

    private static final String USERNAME = "ms2-service-test";
    private static final String PASSWORD = "SenhaServicoTeste123!";

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();

        server = MockRestServiceServer.bindTo(builder).build();

        provider = new UserAuthenticationProvider(
                builder,
                "http://localhost:8082",
                USERNAME,
                PASSWORD
        );
    }

    @Test
    void deveAutenticarUsuarioComCredenciaisValidas() {
        UUID userId = UUID.randomUUID();

        server.expect(requestTo("http://localhost:8082/users/authenticate"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(
                        "Authorization",
                        "Basic " + Base64.getEncoder().encodeToString(
                                (USERNAME + ":" + PASSWORD)
                                        .getBytes(StandardCharsets.UTF_8)
                        )
                ))
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

        var authentication = UsernamePasswordAuthenticationToken.unauthenticated(
                "amanda@email.com",
                "SenhaUsuario123!"
        );

        var result = provider.authenticate(authentication);

        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        assertInstanceOf(AuthenticatedUser.class, result.getPrincipal());
        assertEquals(
                userId,
                ((AuthenticatedUser) result.getPrincipal()).id()
        );

        server.verify();
    }

    @Test
    void deveRejeitarCredenciaisInvalidas() {
        server.expect(requestTo("http://localhost:8082/users/authenticate"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        var authentication = UsernamePasswordAuthenticationToken.unauthenticated(
                "amanda@email.com",
                "senha-incorreta"
        );

        assertThrows(
                BadCredentialsException.class,
                () -> provider.authenticate(authentication)
        );

        server.verify();
    }

    @Test
    void deveLancarExcecaoQuandoServicoDeUsuariosEstiverIndisponivel() {
        server.expect(requestTo("http://localhost:8082/users/authenticate"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        var authentication = UsernamePasswordAuthenticationToken.unauthenticated(
                "amanda@email.com",
                "SenhaUsuario123!"
        );

        assertThrows(
                AuthenticationServiceException.class,
                () -> provider.authenticate(authentication)
        );

        server.verify();
    }

    @Test
    void deveInformarSeSuportaUsernamePasswordAuthenticationToken() {
        assertTrue(provider.supports(
                UsernamePasswordAuthenticationToken.class
        ));
    }
}
