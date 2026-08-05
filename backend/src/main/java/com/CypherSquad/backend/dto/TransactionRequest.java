package com.CypherSquad.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionRequest {
	@NotNull(message = "accountId is required")
	@Positive(message = "accountId must be greater than zero")
	private Long accountId;

	@Positive(message = "payeeId must be greater than zero")
	private Long payeeId;

	@NotNull(message = "amount is required")
	@DecimalMin(value = "0.0001", message = "amount must be greater than zero")
	@Digits(integer = 15, fraction = 4, message = "amount must have up to 15 integer digits and 4 decimal places")
	private BigDecimal amount;

	private LocalDateTime timestamp;

	@Size(max = 10, message = "currency must not exceed 10 characters")
	private String currency;

	@Size(max = 50, message = "type must not exceed 50 characters")
	private String type;

	@Size(max = 50, message = "status must not exceed 50 characters")
	private String status;

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public Long getPayeeId() {
		return payeeId;
	}

	public void setPayeeId(Long payeeId) {
		this.payeeId = payeeId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
