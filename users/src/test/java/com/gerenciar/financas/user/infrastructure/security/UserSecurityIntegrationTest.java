package com.gerenciar.financas.user.infrastructure.security;

import com.gerenciar.financas.user.application.port.in.CreateUserUseCase;
import com.gerenciar.financas.user.domain.model.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "security.service.username=ms2-service-test",
        "security.service.password=SenhaServico123!"
})
class UserSecurityIntegrationTest {

    private static final String USER_PASSWORD = "SenhaUsuario123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CreateUserUseCase createUserUseCase;

    @Test
    void deveRetornar401QuandoUsuarioNaoEstiverAutenticado()
            throws Exception {

        User user = createUser();

        mockMvc.perform(get("/users/{id}", user.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirConsultaDoProprioUsuario()
            throws Exception {

        User user = createUser();

        mockMvc.perform(get("/users/{id}", user.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                basicAuth(user.getEmail(), USER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void deveRetornar403AoConsultarOutroUsuario()
            throws Exception {

        User authenticatedUser = createUser();
        User anotherUser = createUser();

        mockMvc.perform(get("/users/{id}", anotherUser.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                basicAuth(authenticatedUser.getEmail(),
                                        USER_PASSWORD)))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirAtualizacaoDoProprioUsuario()
            throws Exception {

        User user = createUser();

        mockMvc.perform(put("/users/{id}", user.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                basicAuth(user.getEmail(), USER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Nome Atualizado",
                                  "email": "%s"
                                }
                                """.formatted(user.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nome Atualizado"));
    }

    @Test
    void deveRetornar403AoAtualizarOutroUsuario()
            throws Exception {

        User authenticatedUser = createUser();
        User anotherUser = createUser();

        mockMvc.perform(put("/users/{id}", anotherUser.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                basicAuth(authenticatedUser.getEmail(),
                                        USER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Tentativa Indevida",
                                  "email": "%s"
                                }
                                """.formatted(anotherUser.getEmail())))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar403AoExcluirOutroUsuario()
            throws Exception {

        User authenticatedUser = createUser();
        User anotherUser = createUser();

        mockMvc.perform(delete("/users/{id}", anotherUser.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                basicAuth(authenticatedUser.getEmail(),
                                        USER_PASSWORD)))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirExclusaoDoProprioUsuario()
            throws Exception {

        User user = createUser();

        mockMvc.perform(delete("/users/{id}", user.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                basicAuth(user.getEmail(), USER_PASSWORD)))
                .andExpect(status().isNoContent());
    }

    @Test
    void devePermitirListagemParaContaDeServico()
            throws Exception {

        mockMvc.perform(get("/users")
                        .header(HttpHeaders.AUTHORIZATION,
                                basicAuth("ms2-service-test",
                                        "SenhaServico123!")))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornar403QuandoUsuarioTentarListarTodosOsUsuarios()
            throws Exception {

        User user = createUser();

        mockMvc.perform(get("/users")
                        .header(HttpHeaders.AUTHORIZATION,
                                basicAuth(user.getEmail(), USER_PASSWORD)))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirCadastroSemAutenticacao()
            throws Exception {

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Novo Usuário",
                              "email": "novo-%s@email.com",
                              "password": "SenhaUsuario123"
                            }
                            """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated());
    }

    private User createUser() {
        return createUserUseCase.createUser(
                "Usuário de Teste",
                "teste-" + UUID.randomUUID() + "@email.com",
                USER_PASSWORD
        );
    }

    private String basicAuth(String username, String password) {
        String credentials = username + ":" + password;

        return "Basic " + Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8)
        );
    }
}
