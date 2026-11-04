
package com.gerenciar.financas.transactions.adapter.out.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gerenciar.financas.transactions.application.port.out.exchange.ExchangeRateInfo;
import com.gerenciar.financas.transactions.application.port.out.exchange.ExchangeRatePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class ExchangeRateAdapter implements ExchangeRatePort {

    private final RestClient restClient;

    public ExchangeRateAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${services.exchange.base-url}") String exchangeBaseUrl) {

        this.restClient = restClientBuilder
                .baseUrl(exchangeBaseUrl)
                .build();
    }

    @Override
    public Optional<ExchangeRateInfo> getExchangeRate(String currency) {

        if ("BRL".equalsIgnoreCase(currency)) {
            return Optional.of(
                    new ExchangeRateInfo("BRL", BigDecimal.ONE, LocalDate.now())
            );
        }

        try {
            ExchangeResponse response = restClient.get()
                    .uri("/cotacao/{currency}/{date}",
                            currency.toUpperCase(),
                            LocalDate.now())
                    .retrieve()
                    .body(ExchangeResponse.class);

            if (response == null
                    || response.cotacoes() == null
                    || response.cotacoes().isEmpty()) {
                return Optional.empty();
            }

            ExchangeQuote lastQuote =
                    response.cotacoes().get(response.cotacoes().size() - 1);

            return Optional.of(new ExchangeRateInfo(
                    response.moeda(),
                    lastQuote.cotacaoVenda(),
                    LocalDate.parse(response.data())
            ));

        } catch (RestClientException exception) {
            return Optional.empty();
        }
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
