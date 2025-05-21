package com.test.exception;

import com.test.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("APZ000004") // Código estándar para invalid request
                .error("INVALID_REQUEST")
                .timestamp(Instant.now().getEpochSecond()) // ⚠️ Usa segundos, no milisegundos
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("APZ000002")
                .error("BusinessRuleViolation")
                .timestamp(Instant.now().toEpochMilli())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(409).body(errorResponse); // o 400, según el caso
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("APZ000001")
                .error("INTERNAL_SERVER_ERROR")
                .timestamp(Instant.now().getEpochSecond()) // en segundos
                .message("Ha ocurrido un error inesperado.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.internalServerError().body(errorResponse);
    }

    @ExceptionHandler({AuthenticationCredentialsNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleUnauthorized(Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("APZ000007")
                .error("UNAUTHORIZED")
                .timestamp(Instant.now().getEpochSecond())
                .message("Acceso no autorizado")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(CustomerNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .code("APZ000005")
                .error("CUSTOMER_NOT_FOUND")
                .timestamp(Instant.now().toEpochMilli())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidLoanRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidLoanRequest(InvalidLoanRequestException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .code("APZ000006")
                .error("INVALID_LOAN_REQUEST")
                .timestamp(Instant.now().toEpochMilli())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
