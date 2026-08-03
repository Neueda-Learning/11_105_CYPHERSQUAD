package com.CypherSquad.backend.models;

import java.math.BigDecimal;

public class RuleEvaluationResult {
	private Long ruleId;
	private Long transactionId;
	private String ruleType;
	private String comparisonOperator;
	private BigDecimal threshold;
	private BigDecimal transactionAmountUsd;
	private boolean triggered;
	private Long alertId;
	private String message;

	public Long getRuleId() {
		return ruleId;
	}

	public void setRuleId(Long ruleId) {
		this.ruleId = ruleId;
	}

	public Long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	public String getRuleType() {
		return ruleType;
	}

	public void setRuleType(String ruleType) {
		this.ruleType = ruleType;
	}

	public String getComparisonOperator() {
		return comparisonOperator;
	}

	public void setComparisonOperator(String comparisonOperator) {
		this.comparisonOperator = comparisonOperator;
	}

	public BigDecimal getThreshold() {
		return threshold;
	}

	public void setThreshold(BigDecimal threshold) {
		this.threshold = threshold;
	}

	public BigDecimal getTransactionAmountUsd() {
		return transactionAmountUsd;
	}

	public void setTransactionAmountUsd(BigDecimal transactionAmountUsd) {
		this.transactionAmountUsd = transactionAmountUsd;
	}

	public boolean isTriggered() {
		return triggered;
	}

	public void setTriggered(boolean triggered) {
		this.triggered = triggered;
	}

	public Long getAlertId() {
		return alertId;
	}

	public void setAlertId(Long alertId) {
		this.alertId = alertId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}