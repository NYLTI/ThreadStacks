package com.threadstacks.gateway.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiGatewayException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiGatewayException ex) {
	Map<String, Object> errorResponse = new HashMap<>();
	errorResponse.put("error", "API Error");
	errorResponse.put("message", ex.getMessage());
	errorResponse.put("status", ex.getStatus().value());

	return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
	return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
    }
}