package com.CypherSquad.backend.models;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class Rule {

	private Long ruleId;
	private String ruleName;
	private String ruleType;
	private String metric;
	private String comparisonOperator;
	private BigDecimal threshold;
	private BigDecimal minimumThreshold;
	private BigDecimal maximumThreshold;
	private Integer transactionCountThreshold;
	private Integer timeWindowValue;
	private String timeWindowUnit;
	private String payeeScope;
	private String severity;
	private boolean active;
	private Map<String, Object> parameters = new LinkedHashMap<>();

	public Rule() {
	}

	public Long getRuleId() {
		return ruleId;
	}

	public void setRuleId(Long ruleId) {
		this.ruleId = ruleId;
	}

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

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters == null ? new LinkedHashMap<>() : parameters;
	}
}