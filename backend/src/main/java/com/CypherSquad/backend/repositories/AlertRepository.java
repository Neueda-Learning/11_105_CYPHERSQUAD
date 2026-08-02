package com.CypherSquad.backend.repositories;

import com.CypherSquad.backend.models.Alert;

import java.util.List;
import java.util.Optional;

public interface AlertRepository {
	Alert save(Alert alert);

	List<Alert> findAll();

	Optional<Alert> findById(Long alertId);

	List<Alert> findByStatus(String status);

	void deleteById(Long alertId);
}