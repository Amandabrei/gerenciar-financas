package com.gerenciar.financas.user.infrastructure.exception;

import com.gerenciar.financas.user.domain.exception.EmailAlreadyExistsException;
import com.gerenciar.financas.user.domain.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveRetornar404QuandoUsuarioNaoForEncontrado() {
        UserNotFoundException exception =
                new UserNotFoundException("Usuário não encontrado");

        var response = handler.handleUserNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Usuário não encontrado", response.getBody().get("message"));
    }

    @Test
    void deveRetornar409QuandoEmailJaExistir() {
        EmailAlreadyExistsException exception =
                new EmailAlreadyExistsException("E-mail já cadastrado");

        var response = handler.handleEmailAlreadyExists(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("E-mail já cadastrado", response.getBody().get("message"));
    }

    @Test
    void deveRetornar400QuandoArgumentoForInvalido() {
        IllegalArgumentException exception =
                new IllegalArgumentException("Argumento inválido");

        var response = handler.handleIllegalArgument(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Argumento inválido", response.getBody().get("message"));
    }

    @Test
    void deveRetornar409QuandoHouverViolacaoDeIntegridade() {
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("Erro interno do banco");

        var response = handler.handleDataIntegrityViolation(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(
                "Conflito de dados: registro já existe ou viola uma restrição.",
                response.getBody().get("message")
        );
    }

    @Test
    void deveRetornar400QuandoHouverErroDeValidacao() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        var bindingResult = mock(
                org.springframework.validation.BindingResult.class
        );

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of());

        var response = handler.handleValidationErrors(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }
}