package com.CypherSquad.backend.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Map;

public class RuleRequest {
	@NotBlank(message = "ruleName is required")
	@Size(max = 120, message = "ruleName must not exceed 120 characters")
	private String ruleName;

	@NotBlank(message = "ruleType is required")
	@Size(max = 80, message = "ruleType must not exceed 80 characters")
	private String ruleType;

	@Size(max = 80, message = "metric must not exceed 80 characters")
	private String metric;

	@Size(max = 30, message = "comparisonOperator must not exceed 30 characters")
	private String comparisonOperator;

	@Digits(integer = 15, fraction = 4, message = "threshold must have up to 15 integer digits and 4 decimal places")
	private BigDecimal threshold;

	@Digits(integer = 15, fraction = 4, message = "minimumThreshold must have up to 15 integer digits and 4 decimal places")
	private BigDecimal minimumThreshold;

	@Digits(integer = 15, fraction = 4, message = "maximumThreshold must have up to 15 integer digits and 4 decimal places")
	private BigDecimal maximumThreshold;

	@Positive(message = "transactionCountThreshold must be greater than zero")
	private Integer transactionCountThreshold;

	@Positive(message = "timeWindowValue must be greater than zero")
	private Integer timeWindowValue;

	@Size(max = 20, message = "timeWindowUnit must not exceed 20 characters")
	private String timeWindowUnit;

	@Size(max = 40, message = "payeeScope must not exceed 40 characters")
	private String payeeScope;

	@Size(max = 20, message = "severity must not exceed 20 characters")
	private String severity;

	@NotNull(message = "active is required")
	private Boolean active;

	private Map<String, Object> parameters;

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getRuleType() {
		return ruleType;
	}

	public void setRuleType(String ruleType) {
		this.ruleType = ruleType;
	}

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
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

	public BigDecimal getMinimumThreshold() {
		return minimumThreshold;
	}

	public void setMinimumThreshold(BigDecimal minimumThreshold) {
		this.minimumThreshold = minimumThreshold;
	}

	public BigDecimal getMaximumThreshold() {
		return maximumThreshold;
	}

	public void setMaximumThreshold(BigDecimal maximumThreshold) {
		this.maximumThreshold = maximumThreshold;
	}

	public Integer getTransactionCountThreshold() {
		return transactionCountThreshold;
	}

	public void setTransactionCountThreshold(Integer transactionCountThreshold) {
		this.transactionCountThreshold = transactionCountThreshold;
	}

	public Integer getTimeWindowValue() {
		return timeWindowValue;
	}

	public void setTimeWindowValue(Integer timeWindowValue) {
		this.timeWindowValue = timeWindowValue;
	}

	public String getTimeWindowUnit() {
		return timeWindowUnit;
	}

	public void setTimeWindowUnit(String timeWindowUnit) {
		this.timeWindowUnit = timeWindowUnit;
	}

	public String getPayeeScope() {
		return payeeScope;
	}

	public void setPayeeScope(String payeeScope) {
		this.payeeScope = payeeScope;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
}
