package com.gerenciar.financas.transactions.adapter.in.controller;

import com.gerenciar.financas.transactions.adapter.in.dto.AccountBalanceResponse;
import com.gerenciar.financas.transactions.application.port.in.FindAccountBalanceUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@Tag(name = "Saldo", description = "Consulta de saldo das contas")
public class AccountBalanceController {

    private final FindAccountBalanceUseCase findAccountBalanceUseCase;

    public AccountBalanceController(
            FindAccountBalanceUseCase findAccountBalanceUseCase) {
        this.findAccountBalanceUseCase = findAccountBalanceUseCase;
    }

    @GetMapping("/{userId}/balance")
    @Operation(
            summary = "Consultar saldo",
            description = "Retorna o saldo atual do usuário consultado na API Mock."
    )
    public ResponseEntity<AccountBalanceResponse> findBalance(
            @PathVariable UUID userId) {

        var balance = findAccountBalanceUseCase.findBalance(userId);

        return ResponseEntity.ok(
                new AccountBalanceResponse(userId, balance)
        );
    }
}