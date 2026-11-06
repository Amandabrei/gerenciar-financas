package com.gerenciar.financas.transactions.adapter.out.account;

import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AccountFinancialAdapterTest {

    private AccountFinancialAdapter adapter;
    private MockRestServiceServer server;
    private RestClient.Builder builder;

    @BeforeEach
    void setUp() {
        builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        adapter = new AccountFinancialAdapter(
                builder,
                "http://localhost/accounts"
        );
    }

    @Test
    void shouldReturnAccountFinancialInfo() {
        UUID userId = UUID.randomUUID();

        server.expect(requestTo(
                        "http://localhost/accounts?userId=" + userId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        [
                            {
                                "userId": "%s",
                                "balance": 1500.00,
                                "dailyLimit": 5000.00,
                                "transactionLimit": 2000.00,
                                "dailySpent": 300.00
                            }
                        ]
                        """.formatted(userId),
                        MediaType.APPLICATION_JSON
                ));

        AccountFinancialInfo result =
                adapter.getAccountFinancialInfo(userId);

        assertNotNull(result);
        assertEquals(
                0,
                new BigDecimal("1500.00").compareTo(result.balance())
        );
        assertEquals(
                0,
                new BigDecimal("5000.00").compareTo(result.dailyLimit())
        );
        assertEquals(
                0,
                new BigDecimal("2000.00").compareTo(result.transactionLimit())
        );
        assertEquals(
                0,
                new BigDecimal("300.00").compareTo(result.dailySpent())
        );

        server.verify();
    }

    @Test
    void shouldReturnNullWhenAccountDoesNotExist() {
        UUID userId = UUID.randomUUID();

        server.expect(requestTo(
                        "http://localhost/accounts?userId=" + userId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "[]",
                        MediaType.APPLICATION_JSON
                ));

        AccountFinancialInfo result =
                adapter.getAccountFinancialInfo(userId);

        assertNull(result);

        server.verify();
    }

    @Test
    void shouldPropagateExceptionWhenMockApiIsUnavailable() {
        UUID userId = UUID.randomUUID();

        server.expect(requestTo(
                        "http://localhost/accounts?userId=" + userId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThrows(
                RestClientResponseException.class,
                () -> adapter.getAccountFinancialInfo(userId)
        );

        server.verify();
    }
}