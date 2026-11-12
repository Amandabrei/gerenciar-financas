package com.gerenciar.financas.transactions.infrastructure.exception;

import com.gerenciar.financas.transactions.core.exception.AccountNotFoundException;
import com.gerenciar.financas.transactions.core.exception.InvalidTransactionException;
import com.gerenciar.financas.transactions.core.exception.TransactionNotFoundException;
import com.gerenciar.financas.transactions.core.exception.UserNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.context.request.WebRequest;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<Object> handleInvalidTransaction(
            InvalidTransactionException exception) {
        return buildResponse(
                HttpStatus.BAD_REQUEST, exception.getMessage(), List.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(
            IllegalArgumentException exception) {
        return buildResponse(
                HttpStatus.BAD_REQUEST, exception.getMessage(), List.of());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido ou malformado.",
                List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Parâmetro inválido: " + exception.getName() + ".",
                List.of());
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Parâmetro obrigatório ausente: " + exception.getParameterName() + ".",
                List.of());
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return buildResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Método HTTP não permitido para este endpoint.",
                List.of());
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Endpoint não encontrado.",
                List.of());
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Endpoint não encontrado.",
                List.of());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<FieldError> fields = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldError(
                        error.getField(),
                        error.getDefaultMessage()))
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Erro de validação dos campos.",
                fields);
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<Object> handleTransactionNotFound(
            TransactionNotFoundException exception) {
        return buildResponse(
                HttpStatus.NOT_FOUND, exception.getMessage(), List.of());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFound(
            UserNotFoundException exception) {
        return buildResponse(
                HttpStatus.NOT_FOUND, exception.getMessage(), List.of());
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Object> handleAccountNotFound(
            AccountNotFoundException exception) {
        return buildResponse(
                HttpStatus.NOT_FOUND, exception.getMessage(), List.of());
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Object> handleExternalService(
            RestClientException exception) {
        log.error("Falha na comunicação com serviço externo.", exception);

        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Não foi possível comunicar com um serviço externo.",
                List.of());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatus(
            ResponseStatusException exception) {

        HttpStatus status =
                HttpStatus.valueOf(exception.getStatusCode().value());

        return buildResponse(
                status,
                exception.getReason() != null
                        ? exception.getReason()
                        : status.getReasonPhrase(),
                List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(
            Exception exception) {

        log.error("Ocorreu um erro interno inesperado.", exception);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno inesperado.",
                List.of());
    }

    private ResponseEntity<Object> buildResponse(
            HttpStatusCode status,
            String message,
            List<FieldError> fields) {

        ApiError error = new ApiError(
                LocalDateTime.now(clock),
                status.value(),
                status instanceof HttpStatus httpStatus
                        ? httpStatus.getReasonPhrase()
                        : "Erro",
                message,
                fields);

        return ResponseEntity.status(status).body(error);
    }

    public record ApiError(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            List<FieldError> fields
    ) {}

    public record FieldError(
            String field,
            String message
    ) {}
}
