package com.CypherSquad.backend.services;

import com.CypherSquad.backend.models.Alert;
import com.CypherSquad.backend.repositories.AlertRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
		return alertRepository.save(alert);
	}

	@Override
	public List<Alert> getAlerts() {
		return alertRepository.findAll();
	}

	@Override
	public Alert getAlertById(Long alertId) {
		return alertRepository.findById(alertId)
			.orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + alertId));
	}

	@Override
	public List<Alert> getAlertsByStatus(String status) {
		return alertRepository.findByStatus(status);
	}

	@Override
	public Alert updateAlert(Long alertId, Alert alert) {
		Alert existingAlert = getAlertById(alertId);
		alert.setAlertId(existingAlert.getAlertId());
		if (alert.getCreateDate() == null) {
			alert.setCreateDate(existingAlert.getCreateDate());
		}
		return alertRepository.save(alert);
	}

	@Override
	public void deleteAlert(Long alertId) {
		if (alertRepository.findById(alertId).isEmpty()) {
			throw new ResourceNotFoundException("Alert not found: " + alertId);
		}
		alertRepository.deleteById(alertId);
	}

	@Override
	public Alert getAlertByAlertId(Long alertId) {
		return getAlertById(alertId);
	}
}