package com.CypherSquad.backend.repositories;

import com.CypherSquad.backend.models.Alert;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryAlertRepository implements AlertRepository {
	private final ConcurrentMap<Long, Alert> storage = new ConcurrentHashMap<>();
	private final AtomicLong sequence = new AtomicLong(0);

	@Override
	public Alert save(Alert alert) {
		Long alertId = alert.getAlertId();
		if (alertId == null) {
			alertId = sequence.incrementAndGet();
			alert.setAlertId(alertId);
		} else {
			Long resolvedAlertId = alertId;
			sequence.updateAndGet(current -> Math.max(current, resolvedAlertId));
		}
		storage.put(alertId, alert);
		return alert;
	}

	@Override
	public List<Alert> findAll() {
		List<Alert> alerts = new ArrayList<>(storage.values());
		alerts.sort(Comparator.comparing(Alert::getAlertId));
		return alerts;
	}

	@Override
	public Optional<Alert> findById(Long alertId) {
		return Optional.ofNullable(storage.get(alertId));
	}

	@Override
	public List<Alert> findByStatus(String status) {
		if (status == null) {
			return List.of();
		}
		return storage.values().stream()
			.filter(alert -> alert.getStatus() != null && alert.getStatus().equalsIgnoreCase(status))
			.sorted(Comparator.comparing(Alert::getAlertId))
			.collect(Collectors.toList());
	}

	@Override
	public void deleteById(Long alertId) {
		storage.remove(alertId);
	}
}