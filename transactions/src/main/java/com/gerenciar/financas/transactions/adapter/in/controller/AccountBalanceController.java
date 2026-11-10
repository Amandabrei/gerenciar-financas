package com.gerenciar.financas.transactions.adapter.in.controller;

import com.gerenciar.financas.transactions.adapter.in.dto.AccountBalanceResponse;
import com.gerenciar.financas.transactions.application.port.in.FindAccountBalanceUseCase;
import com.gerenciar.financas.transactions.infrastructure.security.AuthenticatedUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
            description = "Retorna o saldo do usuário autenticado na API Mock."
    )
    public ResponseEntity<AccountBalanceResponse> findBalance(
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        if (authenticatedUser == null || !authenticatedUser.id().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem permissão para consultar o saldo de outro usuário."
            );
        }

        var balance = findAccountBalanceUseCase.findBalance(authenticatedUser.id());

        return ResponseEntity.ok(
                new AccountBalanceResponse(authenticatedUser.id(), balance)
        );
    }
}
