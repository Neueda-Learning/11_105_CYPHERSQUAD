package com.CypherSquad.backend.controllers;

import com.CypherSquad.backend.dto.AlertStatusUpdateRequest;
import com.CypherSquad.backend.dto.AlertUpdateRequest;
import com.CypherSquad.backend.models.Alert;
import com.CypherSquad.backend.services.AlertService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alerts")
@CrossOrigin(origins = "*")
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

	@GetMapping("/alertId/{alertId}")
	public ResponseEntity<Alert> getAlertByAlertId(@PathVariable("alertId") Long alertId) {
		return ResponseEntity.ok(alertService.getAlertByAlertId(alertId));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Alert> updateAlert(@PathVariable("id") Long alertId, @Valid @RequestBody AlertUpdateRequest request) {
		return ResponseEntity.ok(alertService.updateAlert(alertId, toAlert(request)));
	}

	@PutMapping("/{id}/status")
	public ResponseEntity<Alert> updateAlertStatus(@PathVariable("id") Long alertId,
		@Valid @RequestBody AlertStatusUpdateRequest request) {
		return ResponseEntity.ok(alertService.updateAlertStatus(alertId, request.getStatus()));
	}

	@GetMapping("/status/{status}")
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

	private Alert toAlert(AlertUpdateRequest request) {
		Alert alert = new Alert();
		alert.setTransactionId(request.getTransactionId());
		alert.setRuleId(request.getRuleId());
		alert.setSeverity(request.getSeverity());
		alert.setStatus(request.getStatus());
		alert.setCreateDate(request.getCreateDate());
		alert.setCloseDate(request.getCloseDate());
		alert.setNote(request.getNote());
		return alert;
	}
}