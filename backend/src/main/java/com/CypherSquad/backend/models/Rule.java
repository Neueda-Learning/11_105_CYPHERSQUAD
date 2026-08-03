package com.CypherSquad.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "rules")
public class Rule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "rule_id")
	private Long ruleId;

	@Column(name = "rule_name", nullable = false, length = 120)
	private String ruleName;

	@Column(name = "rule_type", nullable = false, length = 80)
	private String ruleType;

	@Column(name = "metric", length = 80)
	private String metric;

	@Column(name = "comparison_operator", length = 30)
	private String comparisonOperator;

	@Column(name = "threshold", precision = 19, scale = 4)
	private BigDecimal threshold;

	@Column(name = "minimum_threshold", precision = 19, scale = 4)
	private BigDecimal minimumThreshold;

	@Column(name = "maximum_threshold", precision = 19, scale = 4)
	private BigDecimal maximumThreshold;

	@Column(name = "transaction_count_threshold")
	private Integer transactionCountThreshold;

	@Column(name = "time_window_value")
	private Integer timeWindowValue;

	@Column(name = "time_window_unit", length = 20)
	private String timeWindowUnit;

	@Column(name = "payee_scope", length = 40)
	private String payeeScope;

	@Column(name = "severity", length = 20)
	private String severity;

	@Column(name = "active", nullable = false)
	private boolean active;

	@Lob
	@Column(name = "parameters_json")
	@Convert(converter = RuleParametersConverter.class)
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