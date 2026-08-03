package com.CypherSquad.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

	@GetMapping("/")
	public ResponseEntity<Map<String, String>> home() {
		return ResponseEntity.ok(Map.of(
			"service", "CypherSquad Backend",
			"status", "UP",
			"message", "Use /transactions, /rules, /alerts"
		));
	}

	@GetMapping("/health")
	public ResponseEntity<Map<String, String>> health() {
		return ResponseEntity.ok(Map.of("status", "UP"));
	}
}