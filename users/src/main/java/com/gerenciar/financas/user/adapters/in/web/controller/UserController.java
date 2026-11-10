package com.gerenciar.financas.user.adapters.in.web.controller;

import com.gerenciar.financas.user.adapters.in.dto.CreateUserRequest;
import com.gerenciar.financas.user.adapters.in.dto.UpdateUserRequest;
import com.gerenciar.financas.user.adapters.in.dto.UserResponse;
import com.gerenciar.financas.user.application.port.in.*;
import com.gerenciar.financas.user.domain.model.User;
import com.gerenciar.financas.user.adapters.in.dto.AuthenticateUserRequest;
import com.gerenciar.financas.user.infrastructure.security.AuthenticatedUser;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/users")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final FindAllUsersUseCase findAllUsersUseCase;
    private final FindUserByIdUseCase findUserByIdUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final ImportUsersCsvUseCase importUsersCsvUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;

    public UserController(
            CreateUserUseCase createUserUseCase,
            FindAllUsersUseCase findAllUsersUseCase,
            FindUserByIdUseCase findUserByIdUseCase,
            UpdateUserUseCase updateUserUseCase,
            DeleteUserUseCase deleteUserUseCase,
            ImportUsersCsvUseCase importUsersCsvUseCase,
            AuthenticateUserUseCase authenticateUserUseCase
            ){

        this.createUserUseCase = createUserUseCase;
        this.findAllUsersUseCase = findAllUsersUseCase;
        this.findUserByIdUseCase = findUserByIdUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.importUsersCsvUseCase = importUsersCsvUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @Operation(summary = "Listar usuários")
    @GetMapping
    public ResponseEntity<List<UserResponse>> findAllUsers() {

        List<UserResponse> users = findAllUsersUseCase.findAllUsers()
                .stream()
                .map(UserResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Buscar usuários pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findUserById(
            @PathVariable UUID id,
            Authentication authentication) {

        AuthenticatedUser principal = getAuthenticatedUser(authentication);

        if (!principal.serviceAccount() && !id.equals(principal.id())) {
            throw new AccessDeniedException(
                    "Você não tem permissão para consultar este usuário."
            );
        }

        User user = findUserByIdUseCase.findUserById(id);

        return ResponseEntity.ok(UserResponse.fromDomain(user));
    }

    @Operation(summary = "Cadastrar usuário")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        User user = createUserUseCase.createUser(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );

        UserResponse response = UserResponse.fromDomain(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Atualiza usuário pelo ID")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {

        AuthenticatedUser principal = getAuthenticatedUser(authentication);
        ensureOwnUser(id, principal);

        User user = updateUserUseCase.updateUser(
                id,
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );

        return ResponseEntity.ok(UserResponse.fromDomain(user));
    }

    @Operation(summary = "Excluir usuário")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID id,
            Authentication authentication) {

        AuthenticatedUser principal = getAuthenticatedUser(authentication);
        ensureOwnUser(id, principal);

        deleteUserUseCase.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Importar usuários por arquivo CSV")
    @PostMapping(value = "/import", consumes = "multipart/form-data")
    public ResponseEntity<ImportUsersCsvResult> importUsers(
            @RequestParam("file") MultipartFile file) throws IOException {

        ImportUsersCsvResult result = importUsersCsvUseCase.execute(
                file.getBytes()
        );

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Autenticar usuário")
    @PostMapping("/authenticate")
    public ResponseEntity<UserResponse> authenticate(
            @Valid @RequestBody AuthenticateUserRequest request) {

        User user = authenticateUserUseCase.authenticate(
                request.email(),
                request.password()
        );

        return ResponseEntity.ok(UserResponse.fromDomain(user));
    }

    private AuthenticatedUser getAuthenticatedUser(
            Authentication authentication) {

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof AuthenticatedUser authenticatedUser)) {
            throw new AccessDeniedException(
                    "Usuário autenticado inválido."
            );
        }

        return authenticatedUser;
    }

    private void ensureOwnUser(
            UUID requestedId,
            AuthenticatedUser principal) {

        if (principal.serviceAccount()
                || !requestedId.equals(principal.id())) {

            throw new AccessDeniedException(
                    "Você não tem permissão para alterar este usuário."
            );
        }
    }
}
