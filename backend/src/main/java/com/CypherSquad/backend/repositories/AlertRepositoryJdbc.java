package com.CypherSquad.backend.repositories;

import com.CypherSquad.backend.models.Alert;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class AlertRepositoryJdbc implements AlertRepository {
	private static final RowMapper<Alert> ALERT_ROW_MAPPER = (rs, rowNum) -> {
		Alert alert = new Alert();
		alert.setAlertId(rs.getLong("alert_id"));
		alert.setTransactionId(rs.getObject("transaction_id", Long.class));
		alert.setRuleId(rs.getObject("rule_id", Long.class));
		alert.setSeverity(rs.getString("severity"));
		alert.setStatus(rs.getString("status"));
		Timestamp createDate = rs.getTimestamp("create_date");
		alert.setCreateDate(createDate == null ? null : createDate.toLocalDateTime());
		Timestamp closeDate = rs.getTimestamp("close_date");
		alert.setCloseDate(closeDate == null ? null : closeDate.toLocalDateTime());
		alert.setNote(rs.getString("note"));
		return alert;
	};

	private final JdbcTemplate jdbcTemplate;

	public AlertRepositoryJdbc(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Alert createAlert(Alert alert) {
		String sql = "INSERT INTO alerts (transaction_id, rule_id, severity, status, create_date, close_date, note) VALUES (?, ?, ?, ?, ?, ?, ?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setObject(1, alert.getTransactionId());
			ps.setObject(2, alert.getRuleId());
			ps.setString(3, alert.getSeverity());
			ps.setString(4, alert.getStatus());
			ps.setTimestamp(5, alert.getCreateDate() == null ? null : Timestamp.valueOf(alert.getCreateDate()));
			ps.setTimestamp(6, alert.getCloseDate() == null ? null : Timestamp.valueOf(alert.getCloseDate()));
			ps.setString(7, alert.getNote());
			return ps;
		}, keyHolder);

		Number key = keyHolder.getKey();
		if (key != null) {
			alert.setAlertId(key.longValue());
		}
		return alert;
	}

	@Override
	public List<Alert> getAlerts() {
		String sql = "SELECT alert_id, transaction_id, rule_id, severity, status, create_date, close_date, note FROM alerts WHERE UPPER(COALESCE(status, '')) <> 'DELETED' ORDER BY alert_id";
		return jdbcTemplate.query(sql, ALERT_ROW_MAPPER);
	}

	@Override
	public Optional<Alert> getAlertById(Long alertId) {
		String sql = "SELECT alert_id, transaction_id, rule_id, severity, status, create_date, close_date, note FROM alerts WHERE alert_id = ?";
		List<Alert> alerts = jdbcTemplate.query(sql, ALERT_ROW_MAPPER, alertId);
		return alerts.stream().findFirst();
	}

	@Override
	public List<Alert> getAlertsByStatus(String status) {
		String sql = "SELECT alert_id, transaction_id, rule_id, severity, status, create_date, close_date, note FROM alerts WHERE LOWER(status) = LOWER(?) ORDER BY alert_id";
		return jdbcTemplate.query(sql, ALERT_ROW_MAPPER, status);
	}

	@Override
	public Alert updateAlert(Long alertId, Alert alert) {
		String sql = "UPDATE alerts SET transaction_id = ?, rule_id = ?, severity = ?, status = ?, create_date = ?, close_date = ?, note = ? WHERE alert_id = ?";
		jdbcTemplate.update(sql,
			alert.getTransactionId(),
			alert.getRuleId(),
			alert.getSeverity(),
			alert.getStatus(),
			alert.getCreateDate() == null ? null : Timestamp.valueOf(alert.getCreateDate()),
			alert.getCloseDate() == null ? null : Timestamp.valueOf(alert.getCloseDate()),
			alert.getNote(),
			alertId);
		alert.setAlertId(alertId);
		return alert;
	}

	@Override
	public Alert updateAlertStatus(Long alertId, String status, LocalDateTime closeDate) {
		String sql = "UPDATE alerts SET status = ?, close_date = ? WHERE alert_id = ?";
		jdbcTemplate.update(sql,
			status,
			closeDate == null ? null : Timestamp.valueOf(closeDate),
			alertId);
		return getAlertById(alertId)
			.orElseThrow(() -> new IllegalStateException("Alert not found after update: " + alertId));
	}

	@Override
	public void deleteAlert(Long alertId) {
		String sql = "UPDATE alerts SET status = ?, close_date = ? WHERE alert_id = ?";
		jdbcTemplate.update(sql, "DELETED", Timestamp.valueOf(LocalDateTime.now()), alertId);
	}

	@Override
	public Optional<Alert> getAlertByAlertId(Long alertId) {
		return getAlertById(alertId);
	}
}
