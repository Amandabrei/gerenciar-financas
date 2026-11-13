package com.gerenciar.financas.transactions.adapter.out.exchange;

import com.gerenciar.financas.transactions.application.port.out.exchange.ExchangeRateInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ExchangeRateAdapterTest {

    private ExchangeRateAdapter adapter;
    private MockRestServiceServer server;
    private RestClient.Builder builder;

    @BeforeEach
    void setUp() {
        builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
    }

    @Test
    void shouldReturnLastAvailableQuoteWhenCurrentDateIsWeekend() {

        adapter = new ExchangeRateAdapter(
                builder,
                "http://localhost",
                Clock.fixed(
                        Instant.parse("2026-11-08T12:00:00Z"),
                        ZoneId.of("America/Sao_Paulo")
                )
        );

        server.expect(requestTo(
                        "http://localhost/cotacao/USD/2026-11-06"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                            "moeda": "USD",
                            "data": "2026-11-06",
                            "cotacoes": [
                                {
                                    "cotacao_venda": 5.4321
                                }
                            ]
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        Optional<ExchangeRateInfo> result =
                adapter.getExchangeRate("USD");

        assertTrue(result.isPresent());
        assertEquals("USD", result.get().currency());
        assertEquals(
                0,
                new BigDecimal("5.4321").compareTo(result.get().rate())
        );
        assertEquals(
                LocalDate.of(2026, 11, 6),
                result.get().quoteDate()
        );

        server.verify();
    }

    @Test
    void shouldReturnLastAvailableQuoteWhenCurrentDateIsHoliday() {

        adapter = new ExchangeRateAdapter(
                builder,
                "http://localhost",
                Clock.fixed(
                        Instant.parse("2026-11-02T12:00:00Z"),
                        ZoneId.of("America/Sao_Paulo")
                )
        );

        server.expect(requestTo(
                        "http://localhost/cotacao/EUR/2026-11-02"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

          server.expect(requestTo(
                        "http://localhost/cotacao/EUR/2026-10-30"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                            "moeda": "EUR",
                            "data": "2026-10-30",
                            "cotacoes": [
                                {
                                    "cotacao_venda": 6.1234
                                }
                            ]
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        Optional<ExchangeRateInfo> result =
                adapter.getExchangeRate("EUR");

        assertTrue(result.isPresent());
        assertEquals("EUR", result.get().currency());
        assertEquals(
                0,
                new BigDecimal("6.1234").compareTo(result.get().rate())
        );
        assertEquals(
                LocalDate.of(2026, 10, 30),
                result.get().quoteDate()
        );

        server.verify();
    }


    @Test
    void shouldReuseCachedQuoteWithoutCallingBrasilApiAgain() {

        adapter = new ExchangeRateAdapter(
                builder,
                "http://localhost",
                Clock.fixed(
                        Instant.parse("2026-11-09T12:00:00Z"),
                        ZoneId.of("America/Sao_Paulo")
                )
        );

        server.expect(requestTo(
                        "http://localhost/cotacao/USD/2026-11-09"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                            "moeda": "USD",
                            "data": "2026-11-09",
                            "cotacoes": [
                                {
                                    "cotacao_venda": 5.4321
                                }
                            ]
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        Optional<ExchangeRateInfo> firstResult =
                adapter.getExchangeRate("USD");

        Optional<ExchangeRateInfo> secondResult =
                adapter.getExchangeRate("USD");

        assertTrue(firstResult.isPresent());
        assertEquals(firstResult, secondResult);

        server.verify();
    }

    @Test
    void shouldReuseNegativeCacheWithoutRepeatingFailedRequest() {

        adapter = new ExchangeRateAdapter(
                builder,
                "http://localhost",
                Clock.fixed(
                        Instant.parse("2026-11-09T12:00:00Z"),
                        ZoneId.of("America/Sao_Paulo")
                )
        );

        server.expect(requestTo(
                        "http://localhost/cotacao/USD/2026-11-09"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        Optional<ExchangeRateInfo> firstResult =
                adapter.getExchangeRate("USD");

        Optional<ExchangeRateInfo> secondResult =
                adapter.getExchangeRate("USD");

        assertTrue(firstResult.isEmpty());
        assertTrue(secondResult.isEmpty());

        server.verify();
    }

    @Test
    void shouldReturnBrlWithoutCallingBrasilApi() {
        adapter = new ExchangeRateAdapter(
                builder,
                "http://localhost",
                Clock.fixed(
                        Instant.parse("2026-11-09T12:00:00Z"),
                        ZoneId.of("America/Sao_Paulo")
                )
        );

        Optional<ExchangeRateInfo> result =
                adapter.getExchangeRate("BRL");

        assertTrue(result.isPresent());
        assertEquals("BRL", result.get().currency());
        assertEquals(
                0,
                BigDecimal.ONE.compareTo(result.get().rate())
        );
        assertEquals(
                LocalDate.of(2026, 11, 9),
                result.get().quoteDate()
        );

        server.verify();
    }

    @Test
    void shouldKeepIndependentCacheForEachCurrency() {
        adapter = new ExchangeRateAdapter(
                builder,
                "http://localhost",
                Clock.fixed(
                        Instant.parse("2026-11-09T12:00:00Z"),
                        ZoneId.of("America/Sao_Paulo")
                )
        );

        server.expect(requestTo(
                        "http://localhost/cotacao/USD/2026-11-09"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                            "moeda": "USD",
                            "data": "2026-11-09",
                            "cotacoes": [
                                {
                                    "cotacao_venda": 5.4321
                                }
                            ]
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        server.expect(requestTo(
                        "http://localhost/cotacao/EUR/2026-11-09"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                            "moeda": "EUR",
                            "data": "2026-11-09",
                            "cotacoes": [
                                {
                                    "cotacao_venda": 6.1234
                                }
                            ]
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        Optional<ExchangeRateInfo> usdFirst =
                adapter.getExchangeRate("USD");

        Optional<ExchangeRateInfo> eurFirst =
                adapter.getExchangeRate("EUR");

        Optional<ExchangeRateInfo> usdSecond =
                adapter.getExchangeRate("USD");

        Optional<ExchangeRateInfo> eurSecond =
                adapter.getExchangeRate("EUR");

        assertTrue(usdFirst.isPresent());
        assertTrue(eurFirst.isPresent());

        assertEquals(usdFirst, usdSecond);
        assertEquals(eurFirst, eurSecond);

        assertEquals(
                0,
                new BigDecimal("5.4321").compareTo(usdFirst.get().rate())
        );

        assertEquals(
                0,
                new BigDecimal("6.1234").compareTo(eurFirst.get().rate())
        );

        server.verify();
    }
}
