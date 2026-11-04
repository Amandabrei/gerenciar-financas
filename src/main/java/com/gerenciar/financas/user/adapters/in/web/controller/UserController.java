package com.gerenciar.financas.user.adapters.in.web.controller;

import com.gerenciar.financas.user.adapters.in.dto.CreateUserRequest;
import com.gerenciar.financas.user.adapters.in.dto.UpdateUserRequest;
import com.gerenciar.financas.user.adapters.in.dto.UserResponse;
import com.gerenciar.financas.user.application.port.in.*;
import com.gerenciar.financas.user.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

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

    public UserController(
            CreateUserUseCase createUserUseCase,
            FindAllUsersUseCase findAllUsersUseCase,
            FindUserByIdUseCase findUserByIdUseCase,
            UpdateUserUseCase updateUserUseCase,
            DeleteUserUseCase deleteUserUseCase,
            ImportUsersCsvUseCase importUsersCsvUseCase
            ){

        this.createUserUseCase = createUserUseCase;
        this.findAllUsersUseCase = findAllUsersUseCase;
        this.findUserByIdUseCase = findUserByIdUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.importUsersCsvUseCase = importUsersCsvUseCase;
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
            @PathVariable UUID id) {

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
            @Valid @RequestBody UpdateUserRequest request) {

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
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {

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
}
