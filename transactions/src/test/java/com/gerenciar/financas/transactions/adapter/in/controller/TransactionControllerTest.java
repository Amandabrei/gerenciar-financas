package com.gerenciar.financas.transactions.adapter.in.controller;

import com.gerenciar.financas.transactions.application.port.in.CreateTransactionUseCase;
import com.gerenciar.financas.transactions.application.port.in.FindTransactionUseCase;
import com.gerenciar.financas.transactions.domain.enums.TransactionStatus;
import com.gerenciar.financas.transactions.domain.enums.TransactionType;
import com.gerenciar.financas.transactions.domain.model.Transaction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateTransactionUseCase createTransactionUseCase;

    @MockitoBean
    private FindTransactionUseCase findTransactionUseCase;

    private final UUID ownerId = UUID.randomUUID();
    private final UUID destinationUserId = UUID.randomUUID();
    private final UUID transactionId = UUID.randomUUID();

    private Transaction transaction(TransactionType type, String category) {
        return new Transaction(
                transactionId,
                ownerId,
                type == TransactionType.TRANSFER ? destinationUserId : null,
                type,
                new BigDecimal("100.00"),
                LocalDateTime.of(2026, 11, 5, 15, 0),
                "Transação de teste",
                category,
                TransactionStatus.APPROVED,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Test
    void deveConsultarTransacaoPorId() throws Exception {
        given(findTransactionUseCase.findById(transactionId))
                .willReturn(Optional.of(transaction(TransactionType.DEPOSIT, null)));

        mockMvc.perform(get("/transactions/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void deveRetornar404QuandoTransacaoNaoExistir() throws Exception {
        given(findTransactionUseCase.findById(transactionId))
                .willReturn(Optional.empty());

        mockMvc.perform(get("/transactions/{id}", transactionId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarTransacoesPorUsuario() throws Exception {
        given(findTransactionUseCase.findAllByOwnerId(
                eq(ownerId), isNull(), isNull(), isNull()))
                .willReturn(List.of(transaction(TransactionType.DEPOSIT, null)));

        mockMvc.perform(get("/transactions")
                        .param("ownerId", ownerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"));

        verify(findTransactionUseCase)
                .findAllByOwnerId(ownerId, null, null, null);
    }

    @Test
    void deveCriarDepositoComStatus201ELocation() throws Exception {
        given(createTransactionUseCase.createDeposit(
                eq(ownerId), any(BigDecimal.class), anyString()))
                .willReturn(transaction(TransactionType.DEPOSIT, null));

        mockMvc.perform(post("/transactions/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId": "%s",
                                  "amount": 100.00,
                                  "description": "Depósito de teste"
                                }
                                """.formatted(ownerId)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.endsWith("/transactions/" + transactionId)))
                .andExpect(jsonPath("$.type").value("DEPOSIT"));
    }

    @Test
    void deveCriarSaqueComStatus201() throws Exception {
        given(createTransactionUseCase.createWithdrawal(
                eq(ownerId), any(BigDecimal.class), anyString()))
                .willReturn(transaction(TransactionType.WITHDRAWAL, null));

        mockMvc.perform(post("/transactions/withdrawal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId": "%s",
                                  "amount": 100.00,
                                  "description": "Saque de teste"
                                }
                                """.formatted(ownerId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"));
    }

    @Test
    void deveCriarTransferenciaComStatus201() throws Exception {
        given(createTransactionUseCase.createTransfer(
                eq(ownerId), eq(destinationUserId),
                any(BigDecimal.class), anyString()))
                .willReturn(transaction(TransactionType.TRANSFER, null));

        mockMvc.perform(post("/transactions/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId": "%s",
                                  "destinationUserId": "%s",
                                  "amount": 100.00,
                                  "description": "Transferência de teste"
                                }
                                """.formatted(ownerId, destinationUserId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.destinationUserId")
                        .value(destinationUserId.toString()));
    }

    @Test
    void deveCriarCompraComStatus201() throws Exception {
        given(createTransactionUseCase.createPurchase(
                eq(ownerId), any(BigDecimal.class), anyString(), anyString()))
                .willReturn(transaction(TransactionType.PURCHASE, "ALIMENTACAO"));

        mockMvc.perform(post("/transactions/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId": "%s",
                                  "amount": 100.00,
                                  "description": "Compra de teste",
                                  "category": "ALIMENTACAO"
                                }
                                """.formatted(ownerId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("PURCHASE"))
                .andExpect(jsonPath("$.category").value("ALIMENTACAO"));
    }


    @Test
    void deveRetornar400QuandoOcorreIllegalArgumentException() throws Exception {
        given(createTransactionUseCase.createDeposit(
                eq(ownerId), any(BigDecimal.class), anyString()))
                .willThrow(new IllegalArgumentException("Valor da transação inválido."));

        mockMvc.perform(post("/transactions/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId": "%s",
                                  "amount": 100.00,
                                  "description": "Depósito de teste"
                                }
                                """.formatted(ownerId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Valor da transação inválido."));
    }

    @Test
    void deveRetornar500QuandoOcorreErroInesperado() throws Exception {
        given(createTransactionUseCase.createDeposit(
                eq(ownerId), any(BigDecimal.class), anyString()))
                .willThrow(new RuntimeException("Erro interno de teste."));

        mockMvc.perform(post("/transactions/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId": "%s",
                                  "amount": 100.00,
                                  "description": "Depósito de teste"
                                }
                                """.formatted(ownerId)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message")
                        .value("Ocorreu um erro interno inesperado."));
    }


    @Test
    void deveRetornar400QuandoJsonEstiverMalformado() throws Exception {
        mockMvc.perform(post("/transactions/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId":
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Corpo da requisição inválido ou malformado."));
    }

    @Test
    void deveRetornar400QuandoUuidDaUrlForInvalido() throws Exception {
        mockMvc.perform(get("/transactions/{id}", "uuid-invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Parâmetro inválido: id."));
    }


}
