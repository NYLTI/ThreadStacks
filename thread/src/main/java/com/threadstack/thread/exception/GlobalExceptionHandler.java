package com.threadstack.thread.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolationException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleResourceNotFound(ResourceNotFoundException ex) {
	return Mono.just(buildResponse(HttpStatus.NOT_FOUND, ex.getReason()));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(WebExchangeBindException ex) {
	Map<String, String> errors = new HashMap<>();
	for (FieldError error : ex.getBindingResult().getFieldErrors()) {
	    errors.put(error.getField(), error.getDefaultMessage());
	}
	return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
	Map<String, Object> errorResponse = new HashMap<>();
	errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
	errorResponse.put("error", "Bad Request");
	errorResponse.put("message", "Validation failed");

	Map<String, String> errors = new HashMap<>();
	ex.getConstraintViolations()
		.forEach(violation -> errors.put(violation.getPropertyPath().toString(), violation.getMessage()));

	errorResponse.put("errors", errors);

	return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundExceptions(ResponseStatusException ex) {
	Map<String, String> errorResponse = new HashMap<>();
	errorResponse.put("error", ex.getReason());
	return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGeneralException(Exception ex) {
	return Mono.just(buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
	Map<String, Object> errorResponse = new HashMap<>();
	errorResponse.put("timestamp", LocalDateTime.now());
	errorResponse.put("status", status.value());
	errorResponse.put("error", status.getReasonPhrase());
	errorResponse.put("message", message);
	return new ResponseEntity<>(errorResponse, status);
    }
}
