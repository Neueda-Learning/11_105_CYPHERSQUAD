package com.CypherSquad.backend.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class AlertUpdateRequest {
	@Positive(message = "transactionId must be greater than zero")
	private Long transactionId;

	@Positive(message = "ruleId must be greater than zero")
	private Long ruleId;

	@Size(max = 20, message = "severity must not exceed 20 characters")
	private String severity;

	@Size(max = 20, message = "status must not exceed 20 characters")
	private String status;

	private LocalDateTime createDate;

	private LocalDateTime closeDate;

	@Size(max = 1000, message = "note must not exceed 1000 characters")
	private String note;

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
