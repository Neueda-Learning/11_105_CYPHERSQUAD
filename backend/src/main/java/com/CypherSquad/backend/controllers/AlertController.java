package com.CypherSquad.backend.controllers;

import com.CypherSquad.backend.models.Alert;
import com.CypherSquad.backend.services.AlertService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/alerts")
public class AlertController {
	private final AlertService alertService;

	public AlertController(AlertService alertService) {
		this.alertService = alertService;
	}

	@GetMapping
	public ResponseEntity<List<Alert>> getAlerts() {
		return ResponseEntity.ok(alertService.getAlerts());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Alert> getAlertById(@PathVariable("id") Long alertId) {
		return ResponseEntity.ok(alertService.getAlertById(alertId));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Alert> updateAlert(@PathVariable("id") Long alertId, @RequestBody Alert alert) {
		return ResponseEntity.ok(alertService.updateAlert(alertId, alert));
	}

	@PutMapping("/{id}/status")
	public ResponseEntity<Alert> updateAlertStatus(@PathVariable("id") Long alertId,
		@RequestBody Map<String, String> payload) {
		return ResponseEntity.ok(alertService.updateAlertStatus(alertId, payload.get("status")));
	}

	@GetMapping("/{status:[A-Za-z][A-Za-z0-9_-]*}")
	public ResponseEntity<List<Alert>> getAlertsByStatus(@PathVariable String status) {
		return ResponseEntity.ok(alertService.getAlertsByStatus(status));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteAlert(@PathVariable("id") Long alertId) {
		alertService.deleteAlert(alertId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/alertId/{alertId}")
	public ResponseEntity<Alert> getAlertByAlertId(@PathVariable Long alertId) {
		return ResponseEntity.status(HttpStatus.OK).body(alertService.getAlertByAlertId(alertId));
	}
}