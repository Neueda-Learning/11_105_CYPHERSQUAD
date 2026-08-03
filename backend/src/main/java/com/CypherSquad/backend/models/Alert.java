package com.CypherSquad.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
public class Alert {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "alert_id")
	private Long alertId;

	@Column(name = "transaction_id")
	private Long transactionId;

	@Column(name = "rule_id")
	private Long ruleId;

	@Column(name = "severity", length = 20)
	private String severity;

	@Column(name = "status", length = 20)
	private String status;

	@Column(name = "create_date")
	private LocalDateTime createDate;

	@Column(name = "close_date")
	private LocalDateTime closeDate;

	@Column(name = "note", length = 1000)
	private String note;

	public Alert() {
	}

	public Long getAlertId() {
		return alertId;
	}

	public void setAlertId(Long alertId) {
		this.alertId = alertId;
	}

	public Long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	public Long getRuleId() {
		return ruleId;
	}

	public void setRuleId(Long ruleId) {
		this.ruleId = ruleId;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreateDate() {
		return createDate;
	}

	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}

	public LocalDateTime getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(LocalDateTime closeDate) {
		this.closeDate = closeDate;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}