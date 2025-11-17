package com.example.fitware.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = defaultBody(HttpStatus.BAD_REQUEST, "Validation failed");
        body.put("errors", ex.getBindingResult().getFieldErrors().stream()
            .map(err -> Map.of("field", err.getField(), "message", err.getDefaultMessage()))
            .toList());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        Map<String, Object> body = defaultBody(status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR,
            ex.getReason() != null ? ex.getReason() : "Unexpected error");
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        Map<String, Object> body = defaultBody(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        body.put("detail", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private Map<String, Object> defaultBody(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}
