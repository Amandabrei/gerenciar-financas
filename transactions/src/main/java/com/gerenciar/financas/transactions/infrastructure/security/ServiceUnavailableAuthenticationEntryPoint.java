package com.gerenciar.financas.transactions.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ServiceUnavailableAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public ServiceUnavailableAuthenticationEntryPoint(
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        if (exception instanceof AuthenticationServiceException) {
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            objectMapper.writeValue(
                    response.getOutputStream(),
                    new ApiError(
                            LocalDateTime.now(),
                            HttpStatus.SERVICE_UNAVAILABLE.value(),
                            HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                            "Serviço de usuários indisponível para autenticação.",
                            List.of()
                    )
            );
            return;
        }

        response.sendError(
                HttpStatus.UNAUTHORIZED.value(),
                "E-mail ou senha inválidos."
        );
    }

    private record ApiError(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            List<FieldError> fields
    ) {}

    private record FieldError(
            String field,
            String message
    ) {}
}
