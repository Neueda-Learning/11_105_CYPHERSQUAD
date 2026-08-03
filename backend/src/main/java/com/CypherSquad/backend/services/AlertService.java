package com.CypherSquad.backend.services;

import com.CypherSquad.backend.models.Alert;

import java.util.List;

public interface AlertService {
	Alert createAlert(Alert alert);

	List<Alert> getAlerts();

	Alert getAlertById(Long alertId);

	List<Alert> getAlertsByStatus(String status);

	Alert updateAlert(Long alertId, Alert alert);

	Alert updateAlertStatus(Long alertId, String status);

	void deleteAlert(Long alertId);

	Alert getAlertByAlertId(Long alertId);
}