package com.gerenciar.financas.user.adapters.in.dto;

import com.gerenciar.financas.user.core.model.User;

import java.util.UUID;

public class UserResponse {

    private UUID id;
    private String name;
    private String email;

    public UserResponse(UUID id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public static UserResponse fromDomain(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}