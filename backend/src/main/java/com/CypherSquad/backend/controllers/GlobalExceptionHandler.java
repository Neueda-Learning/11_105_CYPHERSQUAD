package com.CypherSquad.backend.controllers;

import com.CypherSquad.backend.services.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
		return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
		return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(Map.of(
			"timestamp", LocalDateTime.now().toString(),
			"status", status.value(),
			"error", status.getReasonPhrase(),
			"message", message
		));
	}
}