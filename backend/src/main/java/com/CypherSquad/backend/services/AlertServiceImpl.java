package com.CypherSquad.backend.services;

import com.CypherSquad.backend.models.Alert;
import com.CypherSquad.backend.repositories.AlertRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class AlertServiceImpl implements AlertService {
	private final AlertRepository alertRepository;

	public AlertServiceImpl(AlertRepository alertRepository) {
		this.alertRepository = alertRepository;
	}

	@Override
	public Alert createAlert(Alert alert) {
		if (alert.getCreateDate() == null) {
			alert.setCreateDate(LocalDateTime.now());
		}
		if (alert.getStatus() == null) {
			alert.setStatus("OPEN");
		}
		return alertRepository.createAlert(alert);
	}

	@Override
	public List<Alert> getAlerts() {
		return alertRepository.getAlerts();
	}

	@Override
	public Alert getAlertById(Long alertId) {
		return alertRepository.getAlertById(alertId)
			.orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + alertId));
	}

	@Override
	public List<Alert> getAlertsByStatus(String status) {
		return alertRepository.getAlertsByStatus(status);
	}

	@Override
	public Alert updateAlert(Long alertId, Alert alert) {
		Alert existingAlert = getAlertById(alertId);
		alert.setAlertId(existingAlert.getAlertId());
		if (alert.getCreateDate() == null) {
			alert.setCreateDate(existingAlert.getCreateDate());
		}
		return alertRepository.updateAlert(alertId, alert);
	}

	@Override
	public Alert updateAlertStatus(Long alertId, String status) {
		if (status == null || status.isBlank()) {
			throw new IllegalArgumentException("Status is required");
		}

		getAlertById(alertId);
		String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);
		boolean isClosedStatus = "CLOSED".equals(normalizedStatus)
			|| "DISMISSED".equals(normalizedStatus)
			|| "DISSMISSED".equals(normalizedStatus);
		LocalDateTime closeDate = isClosedStatus ? LocalDateTime.now() : null;
		return alertRepository.updateAlertStatus(alertId, normalizedStatus, closeDate);
	}

	@Override
	public void deleteAlert(Long alertId) {
		if (alertRepository.getAlertById(alertId).isEmpty()) {
			throw new ResourceNotFoundException("Alert not found: " + alertId);
		}
		alertRepository.deleteAlert(alertId);
	}

	@Override
	public Alert getAlertByAlertId(Long alertId) {
		return alertRepository.getAlertByAlertId(alertId)
			.orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + alertId));
	}
}