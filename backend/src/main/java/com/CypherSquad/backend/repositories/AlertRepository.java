package com.CypherSquad.backend.repositories;

import com.CypherSquad.backend.models.Alert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AlertRepository {
	Alert createAlert(Alert alert);

	List<Alert> getAlerts();

	Optional<Alert> getAlertById(Long alertId);

	List<Alert> getAlertsByStatus(String status);

	Alert updateAlert(Long alertId, Alert alert);

	Alert updateAlertStatus(Long alertId, String status, LocalDateTime closeDate);

	void deleteAlert(Long alertId);

	Optional<Alert> getAlertByAlertId(Long alertId);
}