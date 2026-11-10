package com.gerenciar.financas.transactions.adapter.in.controller;

import com.gerenciar.financas.transactions.application.port.in.FindAccountBalanceUseCase;
import com.gerenciar.financas.transactions.domain.exception.AccountNotFoundException;
import com.gerenciar.financas.transactions.infrastructure.security.AuthenticatedUser;
import com.gerenciar.financas.transactions.infrastructure.security.SecurityConfig;
import com.gerenciar.financas.transactions.infrastructure.security.UserAuthenticationProvider;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountBalanceController.class)
@Import(SecurityConfig.class)
class AccountBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FindAccountBalanceUseCase findAccountBalanceUseCase;

    @MockitoBean
    private UserAuthenticationProvider userAuthenticationProvider;

    private final UUID userId = UUID.randomUUID();
    private final UUID otherUserId = UUID.randomUUID();

    private RequestPostProcessor authenticatedAs(UUID authenticatedUserId) {
        AuthenticatedUser principal = new AuthenticatedUser(
                authenticatedUserId,
                "Usuário de teste",
                "teste@example.com"
        );

        var auth = UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        return authentication(auth);
    }

    @Test
    void shouldReturn401WhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/accounts/{userId}/balance", userId))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(findAccountBalanceUseCase);
    }

    @Test
    void shouldReturnAccountBalance() throws Exception {
        given(findAccountBalanceUseCase.findBalance(userId))
                .willReturn(new BigDecimal("1500.00"));

        mockMvc.perform(get("/accounts/{userId}/balance", userId)
                        .with(authenticatedAs(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.balance").value(1500.00));

        verify(findAccountBalanceUseCase).findBalance(userId);
    }

    @Test
    void shouldReturn403WhenUserTriesToAccessAnotherUsersBalance() throws Exception {
        mockMvc.perform(get("/accounts/{userId}/balance", otherUserId)
                        .with(authenticatedAs(userId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verifyNoInteractions(findAccountBalanceUseCase);
    }

    @Test
    void shouldReturn404WhenAccountDoesNotExist() throws Exception {
        given(findAccountBalanceUseCase.findBalance(userId))
                .willThrow(new AccountNotFoundException(
                        "Conta financeira não encontrada para o usuário informado."
                ));

        mockMvc.perform(get("/accounts/{userId}/balance", userId)
                        .with(authenticatedAs(userId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn503WhenMockApiIsUnavailable() throws Exception {
        given(findAccountBalanceUseCase.findBalance(userId))
                .willThrow(new ResourceAccessException(
                        "MockAPI indisponível."
                ));

        mockMvc.perform(get("/accounts/{userId}/balance", userId)
                        .with(authenticatedAs(userId)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }
}
