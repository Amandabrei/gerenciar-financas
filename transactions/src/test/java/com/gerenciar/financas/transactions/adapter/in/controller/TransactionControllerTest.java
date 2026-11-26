package com.gerenciar.financas.transactions.adapter.in.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gerenciar.financas.transactions.application.model.ExpenseAnalytics;
import com.gerenciar.financas.transactions.application.port.in.CreateTransactionUseCase;
import com.gerenciar.financas.transactions.application.port.in.ExpenseAnalyticsUseCase;
import com.gerenciar.financas.transactions.application.port.in.FindTransactionUseCase;
import com.gerenciar.financas.transactions.core.enums.TransactionStatus;
import com.gerenciar.financas.transactions.core.enums.TransactionType;
import com.gerenciar.financas.transactions.core.model.Transaction;
import com.gerenciar.financas.transactions.infrastructure.config.TimeConfig;
import com.gerenciar.financas.transactions.infrastructure.security.AuthenticatedUser;
import com.gerenciar.financas.transactions.infrastructure.security.SecurityConfig;
import com.gerenciar.financas.transactions.infrastructure.security.ServiceUnavailableAuthenticationEntryPoint;
import com.gerenciar.financas.transactions.infrastructure.security.UserAuthenticationProvider;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import({
        SecurityConfig.class,
        ServiceUnavailableAuthenticationEntryPoint.class,
        TimeConfig.class
})
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /*
     * O WebMvcTest não está disponibilizando o ObjectMapper
     * exigido pelo ServiceUnavailableAuthenticationEntryPoint.
     * O mock permite carregar o contexto deste teste de controller.
     */
    @MockitoBean
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateTransactionUseCase createTransactionUseCase;

    @MockitoBean
    private FindTransactionUseCase findTransactionUseCase;

    @MockitoBean
    private UserAuthenticationProvider userAuthenticationProvider;

    @MockitoBean
    private ExpenseAnalyticsUseCase expenseAnalyticsUseCase;

    private final UUID ownerId = UUID.randomUUID();
    private final UUID otherUserId = UUID.randomUUID();
    private final UUID destinationUserId = UUID.randomUUID();
    private final UUID transactionId = UUID.randomUUID();

    private RequestPostProcessor authenticatedAs(UUID userId) {
        AuthenticatedUser principal = new AuthenticatedUser(
                userId,
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

    private Transaction transaction(TransactionType type, String category) {
        return transactionFor(ownerId, type, category);
    }

    private Transaction transactionFor(
            UUID transactionOwnerId,
            TransactionType type,
            String category
    ) {
        return new Transaction(
                transactionId,
                transactionOwnerId,
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

    private ExpenseAnalytics expenseAnalytics() {
        return new ExpenseAnalytics(
                new ExpenseAnalytics.CategorySummary(
                        List.of(
                                new ExpenseAnalytics.CategoryExpense(
                                        "ALIMENTACAO",
                                        new BigDecimal("150.00")
                                ),
                                new ExpenseAnalytics.CategoryExpense(
                                        "MORADIA",
                                        new BigDecimal("200.00")
                                )
                        ),
                        new BigDecimal("350.00")
                ),
                List.of(
                        new ExpenseAnalytics.DailyExpense(
                                LocalDate.of(2026, 11, 5),
                                new BigDecimal("150.00")
                        ),
                        new ExpenseAnalytics.DailyExpense(
                                LocalDate.of(2026, 11, 6),
                                new BigDecimal("200.00")
                        )
                ),
                List.of(
                        new ExpenseAnalytics.MonthlyExpense(
                                "2026-11",
                                new BigDecimal("350.00")
                        )
                )
        );
    }

    @Test
    void deveRetornar401QuandoUsuarioNaoEstiverAutenticado() throws Exception {
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveConsultarTransacaoDoUsuarioAutenticado() throws Exception {
        given(findTransactionUseCase.findById(transactionId))
                .willReturn(Optional.of(transaction(TransactionType.DEPOSIT, null)));

        mockMvc.perform(get("/transactions/{id}", transactionId)
                        .with(authenticatedAs(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.usdQuoteAvailable").value(false))
                .andExpect(jsonPath("$.eurQuoteAvailable").value(false))
                .andExpect(jsonPath("$.usdExchangeRate").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.usdQuoteDate").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.eurExchangeRate").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.eurQuoteDate").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void naoDevePermitirConsultarTransacaoDeOutroUsuario() throws Exception {
        given(findTransactionUseCase.findById(transactionId))
                .willReturn(Optional.of(
                        transactionFor(otherUserId, TransactionType.DEPOSIT, null)
                ));

        mockMvc.perform(get("/transactions/{id}", transactionId)
                        .with(authenticatedAs(ownerId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404QuandoTransacaoNaoExistir() throws Exception {
        given(findTransactionUseCase.findById(transactionId))
                .willReturn(Optional.empty());

        mockMvc.perform(get("/transactions/{id}", transactionId)
                        .with(authenticatedAs(ownerId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarTransacoesDoUsuarioAutenticado() throws Exception {
        given(findTransactionUseCase.findAllByOwnerId(
                eq(ownerId), isNull(), isNull(), isNull()))
                .willReturn(List.of(transaction(TransactionType.DEPOSIT, null)));

        mockMvc.perform(get("/transactions")
                        .with(authenticatedAs(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"));

        verify(findTransactionUseCase)
                .findAllByOwnerId(ownerId, null, null, null);
    }

    @Test
    void deveRetornar401QuandoUsuarioNaoEstiverAutenticadoAoExportarRelatorio()
            throws Exception {

        mockMvc.perform(get("/transactions/report")
                        .param("startDate", "2026-11-01")
                        .param("endDate", "2026-11-10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveExportarRelatorioCsvDoUsuarioAutenticado() throws Exception {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 10);

        given(findTransactionUseCase.findAllByOwnerId(
                eq(ownerId),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                isNull()
        )).willReturn(List.of(
                transaction(TransactionType.PURCHASE, "ALIMENTACAO")
        ));

        var result = mockMvc.perform(get("/transactions/report")
                        .with(authenticatedAs(ownerId))
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string(
                        "Content-Disposition",
                        "attachment; filename=\"relatorio-transacoes.csv\""
                ))
                .andReturn();

        String csv = result.getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("\uFEFF"));
        assertTrue(csv.contains("data;tipo;categoria;descricao;valor_brl;status;motivo_rejeicao"));
        assertTrue(csv.contains("PURCHASE"));
        assertTrue(csv.contains("ALIMENTACAO"));

        verify(findTransactionUseCase).findAllByOwnerId(
                ownerId,
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59, 999_999_999),
                null
        );
    }

    @Test
    void naoDeveExportarRelatorioDeOutroUsuario() throws Exception {
        given(findTransactionUseCase.findAllByOwnerId(
                eq(ownerId),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                isNull()
        )).willReturn(List.of());

        mockMvc.perform(get("/transactions/report")
                        .with(authenticatedAs(ownerId))
                        .param("startDate", "2026-11-01")
                        .param("endDate", "2026-11-10"))
                .andExpect(status().isOk());

        verify(findTransactionUseCase).findAllByOwnerId(
                eq(ownerId),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                isNull()
        );

        verify(findTransactionUseCase, org.mockito.Mockito.never())
                .findAllByOwnerId(
                        eq(otherUserId),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void deveRetornar400QuandoDataInicialForPosteriorADataFinal()
            throws Exception {

        mockMvc.perform(get("/transactions/report")
                        .with(authenticatedAs(ownerId))
                        .param("startDate", "2026-11-10")
                        .param("endDate", "2026-11-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("A data inicial não pode ser posterior à data final."));

        verify(findTransactionUseCase, org.mockito.Mockito.never())
                .findAllByOwnerId(
                        any(UUID.class),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void deveCriarDepositoComStatus201ELocation() throws Exception {
        given(createTransactionUseCase.createDeposit(
                eq(ownerId), any(BigDecimal.class), anyString()))
                .willReturn(transaction(TransactionType.DEPOSIT, null));

        mockMvc.perform(post("/transactions/deposits")
                        .with(authenticatedAs(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 100.00,
                                  "description": "Depósito de teste"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.endsWith("/transactions/" + transactionId)))
                .andExpect(jsonPath("$.type").value("DEPOSIT"));

        verify(createTransactionUseCase)
                .createDeposit(eq(ownerId), any(BigDecimal.class), anyString());
    }

    @Test
    void deveCriarSaqueComStatus201() throws Exception {
        given(createTransactionUseCase.createWithdrawal(
                eq(ownerId), any(BigDecimal.class), anyString()))
                .willReturn(transaction(TransactionType.WITHDRAWAL, null));

        mockMvc.perform(post("/transactions/withdrawal")
                        .with(authenticatedAs(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 100.00,
                                  "description": "Saque de teste"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"));

        verify(createTransactionUseCase)
                .createWithdrawal(eq(ownerId), any(BigDecimal.class), anyString());
    }

    @Test
    void deveCriarTransferenciaComStatus201() throws Exception {
        given(createTransactionUseCase.createTransfer(
                eq(ownerId), eq(destinationUserId),
                any(BigDecimal.class), anyString()))
                .willReturn(transaction(TransactionType.TRANSFER, null));

        mockMvc.perform(post("/transactions/transfers")
                        .with(authenticatedAs(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "destinationUserId": "%s",
                                  "amount": 100.00,
                                  "description": "Transferência de teste"
                                }
                                """.formatted(destinationUserId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.destinationUserId")
                        .value(destinationUserId.toString()));

        verify(createTransactionUseCase).createTransfer(
                eq(ownerId), eq(destinationUserId),
                any(BigDecimal.class), anyString());
    }

    @Test
    void deveCriarCompraComStatus201() throws Exception {
        given(createTransactionUseCase.createPurchase(
                eq(ownerId), any(BigDecimal.class), anyString(), anyString()))
                .willReturn(transaction(TransactionType.PURCHASE, "ALIMENTACAO"));

        mockMvc.perform(post("/transactions/purchase")
                        .with(authenticatedAs(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 100.00,
                                  "description": "Compra de teste",
                                  "category": "ALIMENTACAO"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("PURCHASE"))
                .andExpect(jsonPath("$.category").value("ALIMENTACAO"));

        verify(createTransactionUseCase).createPurchase(
                eq(ownerId), any(BigDecimal.class), anyString(), anyString());
    }

    @Test
    void deveUsarUsuarioAutenticadoMesmoQueJsonInformeOutroOwnerId() throws Exception {
        given(createTransactionUseCase.createDeposit(
                eq(ownerId), any(BigDecimal.class), anyString()))
                .willReturn(transaction(TransactionType.DEPOSIT, null));

        mockMvc.perform(post("/transactions/deposits")
                        .with(authenticatedAs(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId": "%s",
                                  "amount": 100.00,
                                  "description": "Depósito de teste"
                                }
                                """.formatted(otherUserId)))
                .andExpect(status().isCreated());

        verify(createTransactionUseCase)
                .createDeposit(eq(ownerId), any(BigDecimal.class), anyString());
    }

    @Test
    void deveRetornar400QuandoOcorreIllegalArgumentException() throws Exception {
        given(createTransactionUseCase.createDeposit(
                eq(ownerId), any(BigDecimal.class), anyString()))
                .willThrow(new IllegalArgumentException("Valor da transação inválido."));

        mockMvc.perform(post("/transactions/deposits")
                        .with(authenticatedAs(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 100.00,
                                  "description": "Depósito de teste"
                                }
                                """))
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
                        .with(authenticatedAs(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 100.00,
                                  "description": "Depósito de teste"
                                }
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message")
                        .value("Ocorreu um erro interno inesperado."));
    }

    @Test
    void deveRetornar400QuandoJsonEstiverMalformado() throws Exception {
        mockMvc.perform(post("/transactions/deposits")
                        .with(authenticatedAs(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount":
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Corpo da requisição inválido ou malformado."));
    }

    @Test
    void deveRetornar400QuandoUuidDaUrlForInvalido() throws Exception {
        mockMvc.perform(get("/transactions/{id}", "uuid-invalido")
                        .with(authenticatedAs(ownerId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Parâmetro inválido: id."));
    }

    @Test
    void deveAnalisarDespesasPorCategoriaDoUsuarioAutenticado() throws Exception {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        given(expenseAnalyticsUseCase.analyze(ownerId, startDate, endDate))
                .willReturn(expenseAnalytics());

        mockMvc.perform(get("/transactions/analytics/category")
                        .with(authenticatedAs(ownerId))
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(350.00))
                .andExpect(jsonPath("$.categories.length()").value(2))
                .andExpect(jsonPath("$.categories[0].category").value("ALIMENTACAO"))
                .andExpect(jsonPath("$.categories[0].total").value(150.00))
                .andExpect(jsonPath("$.categories[1].category").value("MORADIA"))
                .andExpect(jsonPath("$.categories[1].total").value(200.00));

        verify(expenseAnalyticsUseCase).analyze(ownerId, startDate, endDate);
    }

    @Test
    void deveAnalisarDespesasPorDiaDoUsuarioAutenticado() throws Exception {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        given(expenseAnalyticsUseCase.analyze(ownerId, startDate, endDate))
                .willReturn(expenseAnalytics());

        mockMvc.perform(get("/transactions/analytics/day")
                        .with(authenticatedAs(ownerId))
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].date").value("2026-11-05"))
                .andExpect(jsonPath("$[0].total").value(150.00))
                .andExpect(jsonPath("$[1].date").value("2026-11-06"))
                .andExpect(jsonPath("$[1].total").value(200.00));

        verify(expenseAnalyticsUseCase).analyze(ownerId, startDate, endDate);
    }

    @Test
    void deveAnalisarDespesasPorMesDoUsuarioAutenticado() throws Exception {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        given(expenseAnalyticsUseCase.analyze(ownerId, startDate, endDate))
                .willReturn(expenseAnalytics());

        mockMvc.perform(get("/transactions/analytics/month")
                        .with(authenticatedAs(ownerId))
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].month").value("2026-11"))
                .andExpect(jsonPath("$[0].total").value(350.00));

        verify(expenseAnalyticsUseCase).analyze(ownerId, startDate, endDate);
    }

    @Test
    void deveRetornar401NasRotasDeAnaliseQuandoUsuarioNaoEstiverAutenticado()
            throws Exception {

        mockMvc.perform(get("/transactions/analytics/category"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/transactions/analytics/day"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/transactions/analytics/month"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(expenseAnalyticsUseCase);
    }

    @Test
    void deveRetornar400NaAnaliseQuandoInformarSomenteUmaData()
            throws Exception {

        given(expenseAnalyticsUseCase.analyze(
                any(UUID.class),
                nullable(LocalDate.class),
                nullable(LocalDate.class)
        )).willThrow(new IllegalArgumentException(
                "Informe as duas datas: startDate e endDate."
        ));

        mockMvc.perform(get("/transactions/analytics/category")
                        .with(authenticatedAs(ownerId))
                        .param("startDate", "2026-11-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(get("/transactions/analytics/day")
                        .with(authenticatedAs(ownerId))
                        .param("endDate", "2026-11-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(get("/transactions/analytics/month")
                        .with(authenticatedAs(ownerId))
                        .param("startDate", "2026-11-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(expenseAnalyticsUseCase, org.mockito.Mockito.times(3))
                .analyze(eq(ownerId), any(), any());
    }

    @Test
    void deveRetornar400QuandoParametroObrigatorioEstiverAusente()
            throws Exception {

        mockMvc.perform(get("/transactions/report")
                        .with(authenticatedAs(ownerId))
                        .param("startDate", "2026-11-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Parâmetro obrigatório ausente: endDate."));
    }

    @Test
    void deveRetornar405QuandoMetodoHttpNaoForPermitido()
            throws Exception {

        mockMvc.perform(post("/transactions")
                        .with(authenticatedAs(ownerId)))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void deveRetornar404QuandoEndpointNaoExistir()
            throws Exception {

        mockMvc.perform(get("/transactions/rota-inexistente/recurso")
                        .with(authenticatedAs(ownerId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Endpoint não encontrado."));
    }

    @Test
    void deveRetornarIndicadoresDeCotacaoQuandoTaxasEstiveremDisponiveis() throws Exception {
        LocalDate quoteDate = LocalDate.of(2026, 11, 12);

        Transaction transactionWithQuotes = new Transaction(
                transactionId,
                ownerId,
                null,
                TransactionType.DEPOSIT,
                new BigDecimal("100.00"),
                LocalDateTime.of(2026, 11, 13, 10, 0),
                "Depósito de teste",
                null,
                TransactionStatus.APPROVED,
                null,
                null,
                new BigDecimal("5.40"),
                quoteDate,
                new BigDecimal("6.20"),
                quoteDate,
                new BigDecimal("500.00"),
                new BigDecimal("600.00")
        );

        given(findTransactionUseCase.findById(transactionId))
                .willReturn(Optional.of(transactionWithQuotes));

        mockMvc.perform(get("/transactions/{id}", transactionId)
                        .with(authenticatedAs(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usdQuoteAvailable").value(true))
                .andExpect(jsonPath("$.usdExchangeRate").value(5.40))
                .andExpect(jsonPath("$.usdQuoteDate").value("2026-11-12"))
                .andExpect(jsonPath("$.eurQuoteAvailable").value(true))
                .andExpect(jsonPath("$.eurExchangeRate").value(6.20))
                .andExpect(jsonPath("$.eurQuoteDate").value("2026-11-12"));
    }
}