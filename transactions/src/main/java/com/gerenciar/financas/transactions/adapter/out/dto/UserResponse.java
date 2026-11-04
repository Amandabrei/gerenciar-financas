package com.gerenciar.financas.transactions.adapter.out.dto;

import java.util.UUID;

public class UserResponse {

    private UUID id;
    private String name;
    private String email;

    public UserResponse() {
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
