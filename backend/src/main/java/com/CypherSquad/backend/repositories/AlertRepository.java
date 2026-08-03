package com.CypherSquad.backend.repositories;

import com.CypherSquad.backend.models.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
	List<Alert> findByStatusIgnoreCase(String status);
}