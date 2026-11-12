package com.gerenciar.financas.transactions.adapter.out.account;

import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialInfo;
import com.gerenciar.financas.transactions.application.port.out.account.AccountFinancialPort;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
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

        AccountResponse account = findAccount(userId);

        if (account == null) {
            return null;
        }

        return new AccountFinancialInfo(
                account.balance(),
                account.dailyLimit(),
                account.transactionLimit(),
                account.dailySpent()
        );
    }

    @Override
    public void updateBalance(UUID userId, BigDecimal newBalance) {

        AccountResponse account = findAccount(userId);

        if (account == null || account.id() == null || account.id().isBlank()) {
            throw new IllegalStateException(
                    "Conta financeira não encontrada para atualizar o saldo."
            );
        }

        restClient.patch()
                .uri("/{id}", account.id())
                .body(new BalanceUpdateRequest(newBalance))
                .retrieve()
                .toBodilessEntity();
    }

    private AccountResponse findAccount(UUID userId) {

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
                .orElse(null);
    }

    public record AccountResponse(
            String userId,
            BigDecimal balance,
            BigDecimal dailyLimit,
            BigDecimal transactionLimit,
            BigDecimal dailySpent,
            String id
    ) {
    }

    public record BalanceUpdateRequest(BigDecimal balance) {
    }
}
