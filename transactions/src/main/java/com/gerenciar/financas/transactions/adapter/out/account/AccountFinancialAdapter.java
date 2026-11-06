package com.gerenciar.financas.transactions.adapter.out.account;

import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialInfo;
import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialPort;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Arrays;
import java.util.UUID;

@Component
public class AccountFinancialAdapter implements AccountFinancialPort {

    private final RestClient restClient;

    public AccountFinancialAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${services.account.base-url}") String accountBaseUrl) {

        this.restClient = restClientBuilder
                .baseUrl(accountBaseUrl)
                .build();
    }

    @Override
    public AccountFinancialInfo getAccountFinancialInfo(UUID userId) {

        AccountResponse[] accounts;

        try {
            accounts = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("userId", userId)
                            .build())
                    .retrieve()
                    .body(AccountResponse[].class);

        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                return null;
            }

            throw exception;
        }

        if (accounts == null || accounts.length == 0) {
            return null;
        }

        return Arrays.stream(accounts)
                .filter(account -> userId.toString().equals(account.userId()))
                .findFirst()
                .map(account -> new AccountFinancialInfo(
                        account.balance(),
                        account.dailyLimit(),
                        account.transactionLimit(),
                        account.dailySpent()
                ))
                .orElse(null);
    }

    public record AccountResponse(
            String userId,
            java.math.BigDecimal balance,
            java.math.BigDecimal dailyLimit,
            java.math.BigDecimal transactionLimit,
            java.math.BigDecimal dailySpent
    ) {
    }
}