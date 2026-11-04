package com.gerenciar.financas.user.adapters.in.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonSetter;

public class CreateUserRequest {

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
    private String name;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Informe um e-mail válido")
    @Size(max = 180, message = "O e-mail deve ter no máximo 180 caracteres")
    private String email;

    @JsonSetter("email")
    public void setEmail(String email) {
        this.email = email == null
                ? null
                : email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres")
    private String password;

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
