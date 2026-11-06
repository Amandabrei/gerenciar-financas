package com.gerenciar.financas.transactions.adapter.out.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gerenciar.financas.transactions.application.port.out.exchange.ExchangeRateInfo;
import com.gerenciar.financas.transactions.application.port.out.exchange.ExchangeRatePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class ExchangeRateAdapter implements ExchangeRatePort {

    private static final int MAX_LOOKBACK_DAYS = 10;

    private final RestClient restClient;
    private final Clock clock;

    @Autowired
    public ExchangeRateAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${services.exchange.base-url}") String exchangeBaseUrl) {

        this(
                restClientBuilder,
                exchangeBaseUrl,
                Clock.systemDefaultZone()
        );
    }

    ExchangeRateAdapter(
            RestClient.Builder restClientBuilder,
            String exchangeBaseUrl,
            Clock clock) {

        this.restClient = restClientBuilder
                .baseUrl(exchangeBaseUrl)
                .build();

        this.clock = clock;
    }

    @Override
    public Optional<ExchangeRateInfo> getExchangeRate(String currency) {

        LocalDate today = LocalDate.now(clock);

        if ("BRL".equalsIgnoreCase(currency)) {
            return Optional.of(
                    new ExchangeRateInfo("BRL", BigDecimal.ONE, today)
            );
        }

        LocalDate date = today;

        for (int attempt = 0; attempt <= MAX_LOOKBACK_DAYS; attempt++) {

            try {
                ExchangeResponse response = restClient.get()
                        .uri("/cotacao/{currency}/{date}",
                                currency.toUpperCase(Locale.ROOT),
                                date)
                        .retrieve()
                        .body(ExchangeResponse.class);

                if (response != null
                        && response.cotacoes() != null
                        && !response.cotacoes().isEmpty()) {

                    ExchangeQuote lastQuote =
                            response.cotacoes().get(response.cotacoes().size() - 1);

                    return Optional.of(new ExchangeRateInfo(
                            response.moeda(),
                            lastQuote.cotacaoVenda(),
                            LocalDate.parse(response.data())
                    ));
                }

            } catch (RestClientResponseException exception) {

                if (exception.getStatusCode() != HttpStatus.NOT_FOUND) {
                    return Optional.empty();
                }

            } catch (RestClientException exception) {
                return Optional.empty();
            }

            date = date.minusDays(1);
        }

        return Optional.empty();
    }

    private record ExchangeResponse(
            String moeda,
            String data,
            List<ExchangeQuote> cotacoes
    ) {
    }

    private record ExchangeQuote(
            @JsonProperty("cotacao_venda")
            BigDecimal cotacaoVenda
    ) {
    }
}
