package com.gerenciar.financas.transactions.adapter.in.controller;

import com.gerenciar.financas.transactions.application.port.in.FindAccountBalanceUseCase;
import com.gerenciar.financas.transactions.domain.exception.AccountNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountBalanceController.class)
class AccountBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FindAccountBalanceUseCase findAccountBalanceUseCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void shouldReturnAccountBalance() throws Exception {
        given(findAccountBalanceUseCase.findBalance(userId))
                .willReturn(new BigDecimal("1500.00"));

        mockMvc.perform(get("/accounts/{userId}/balance", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.balance").value(1500.00));
    }

    @Test
    void shouldReturn404WhenAccountDoesNotExist() throws Exception {
        given(findAccountBalanceUseCase.findBalance(userId))
                .willThrow(new AccountNotFoundException(
                        "Conta financeira não encontrada para o usuário informado."
                ));

        mockMvc.perform(get("/accounts/{userId}/balance", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn503WhenMockApiIsUnavailable() throws Exception {
        given(findAccountBalanceUseCase.findBalance(userId))
                .willThrow(new ResourceAccessException(
                        "MockAPI indisponível."
                ));

        mockMvc.perform(get("/accounts/{userId}/balance", userId))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }
}