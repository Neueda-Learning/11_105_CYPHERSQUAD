package com.CypherSquad.backend.services;

import com.CypherSquad.backend.models.Alert;
import com.CypherSquad.backend.models.Rule;
import com.CypherSquad.backend.models.RuleEvaluationResult;
import com.CypherSquad.backend.models.Transaction;
import com.CypherSquad.backend.repositories.RuleRepository;
import com.CypherSquad.backend.repositories.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class RuleServiceImpl implements RuleService {
	private final RuleRepository ruleRepository;
	private final AlertService alertService;
	private final TransactionRepository transactionRepository;

	public RuleServiceImpl(RuleRepository ruleRepository,
		AlertService alertService,
		TransactionRepository transactionRepository) {
		this.ruleRepository = ruleRepository;
		this.alertService = alertService;
		this.transactionRepository = transactionRepository;
	}

	@Override
	public Rule createRule(Rule rule) {
		normalizeRule(rule);
		validateRule(rule);
		return ruleRepository.save(rule);
	}

	@Override
	public List<Rule> getRules() {
		return ruleRepository.findAll();
	}

	@Override
	public Rule getRuleById(Long ruleId) {
		return ruleRepository.findById(ruleId)
			.orElseThrow(() -> new ResourceNotFoundException("Rule not found: " + ruleId));
	}

	@Override
	public Rule updateRule(Long ruleId, Rule rule) {
		Rule existingRule = getRuleById(ruleId);
		rule.setRuleId(existingRule.getRuleId());
		normalizeRule(rule);
		validateRule(rule);
		return ruleRepository.save(rule);
	}

	@Override
	public void deleteRule(Long ruleId) {
		if (ruleRepository.findById(ruleId).isEmpty()) {
			throw new ResourceNotFoundException("Rule not found: " + ruleId);
		}
		ruleRepository.deleteById(ruleId);
	}

	@Override
	public RuleEvaluationResult evaluateRuleForTransaction(Long ruleId, Long transactionId) {
		Rule rule = getRuleById(ruleId);
		Transaction transaction = getTransactionById(transactionId);
		normalizeRule(rule);
		validateRule(rule);
		return evaluateAndBuildResult(rule, transaction);
	}

	@Override
	public List<RuleEvaluationResult> evaluateActiveRulesForTransaction(Long transactionId) {
		Transaction transaction = getTransactionById(transactionId);
		List<Rule> activeRules = ruleRepository.findByActiveTrue();
		List<RuleEvaluationResult> results = new ArrayList<>();

		for (Rule rule : activeRules) {
			normalizeRule(rule);
			validateRule(rule);
			results.add(evaluateAndBuildResult(rule, transaction));
		}

		return results;
	}

	private RuleEvaluationResult evaluateAndBuildResult(Rule rule, Transaction transaction) {
		String ruleType = normalizedValue(rule.getRuleType());
		EvaluationPayload payload = evaluateByRuleType(rule, transaction, ruleType);

		RuleEvaluationResult result = new RuleEvaluationResult();
		result.setRuleId(rule.getRuleId());
		result.setTransactionId(transaction.getTransactionId());
		result.setRuleType(ruleType);
		result.setComparisonOperator(normalizeOperator(rule.getComparisonOperator()));
		result.setThreshold(rule.getThreshold());
		result.setTransactionAmountUsd(transaction.getAmountUsd());
		result.setTriggered(payload.triggered());

		if (payload.triggered()) {
			Alert alert = new Alert();
			alert.setTransactionId(transaction.getTransactionId());
			alert.setRuleId(rule.getRuleId());
			alert.setSeverity(rule.getSeverity() == null ? "MEDIUM" : rule.getSeverity());
			alert.setNote(payload.message());
			Alert createdAlert = alertService.createAlert(alert);
			result.setAlertId(createdAlert.getAlertId());
			result.setMessage("Rule triggered and alert created: " + payload.message());
		} else {
			result.setMessage("Rule not triggered: " + payload.message());
		}

		return result;
	}

	private EvaluationPayload evaluateByRuleType(Rule rule, Transaction transaction, String ruleType) {
		return switch (ruleType) {
			case "AMOUNT_THRESHOLD" -> evaluateAmountThresholdRule(rule, transaction);
			case "AMOUNT_RANGE" -> evaluateAmountRangeRule(rule, transaction);
			case "COUNT_IN_TIME_WINDOW" -> evaluateCountInTimeWindowRule(rule, transaction);
			case "DAILY_LIMIT" -> evaluateDailyLimitRule(rule, transaction);
			case "NEW_PAYEE" -> evaluateNewPayeeRule(rule, transaction);
			default -> evaluateCustomRule(rule, transaction);
		};
	}

	private EvaluationPayload evaluateAmountThresholdRule(Rule rule, Transaction transaction) {
		BigDecimal transactionAmountUsd = requireTransactionAmountUsd(transaction);
		BigDecimal threshold = requireThreshold(rule);
		String operator = normalizeOperator(rule.getComparisonOperator());
		boolean triggered = evaluateComparison(transactionAmountUsd, threshold, operator);
		String message = "amountUsd=" + transactionAmountUsd + " " + operator + " threshold=" + threshold;
		return new EvaluationPayload(triggered, message);
	}

	private EvaluationPayload evaluateAmountRangeRule(Rule rule, Transaction transaction) {
		BigDecimal transactionAmountUsd = requireTransactionAmountUsd(transaction);
		if (rule.getMinimumThreshold() == null || rule.getMaximumThreshold() == null) {
			throw new IllegalArgumentException("AMOUNT_RANGE requires minimumThreshold and maximumThreshold");
		}
		BigDecimal min = rule.getMinimumThreshold();
		BigDecimal max = rule.getMaximumThreshold();
		boolean inclusive = getBooleanParameter(rule.getParameters(), "inclusive", true);
		boolean triggered;

		if (inclusive) {
			triggered = transactionAmountUsd.compareTo(min) >= 0 && transactionAmountUsd.compareTo(max) <= 0;
		} else {
			triggered = transactionAmountUsd.compareTo(min) > 0 && transactionAmountUsd.compareTo(max) < 0;
		}

		String message = "amountUsd=" + transactionAmountUsd + " range=" + min + ".." + max + " inclusive=" + inclusive;
		return new EvaluationPayload(triggered, message);
	}

	private EvaluationPayload evaluateCountInTimeWindowRule(Rule rule, Transaction transaction) {
		Long accountId = requireAccountId(transaction);
		LocalDateTime referenceTime = requireTransactionTimestamp(transaction);
		int windowValue = requirePositive(rule.getTimeWindowValue(), "timeWindowValue");
		String unit = requireTimeWindowUnit(rule.getTimeWindowUnit());
		int countThreshold = requirePositive(rule.getTransactionCountThreshold(), "transactionCountThreshold");

		LocalDateTime start = subtractWindow(referenceTime, windowValue, unit);
		long count = transactionRepository.countByAccountIdAndTimestampBetween(accountId, start, referenceTime);
		BigDecimal observed = BigDecimal.valueOf(count);
		String operator = normalizeOperator(rule.getComparisonOperator());
		boolean triggered = evaluateComparison(observed, BigDecimal.valueOf(countThreshold), operator);

		String message = "count=" + count + " in " + windowValue + " " + unit + " for accountId=" + accountId;
		return new EvaluationPayload(triggered, message);
	}

	private EvaluationPayload evaluateDailyLimitRule(Rule rule, Transaction transaction) {
		Long accountId = requireAccountId(transaction);
		LocalDateTime referenceTime = requireTransactionTimestamp(transaction);
		LocalDate day = referenceTime.toLocalDate();
		LocalDateTime start = day.atStartOfDay();
		LocalDateTime end = day.plusDays(1).atStartOfDay().minusNanos(1);

		String metric = normalizedValue(rule.getMetric());
		String operator = normalizeOperator(rule.getComparisonOperator());

		if ("TRANSACTION_COUNT".equals(metric)) {
			int countThreshold = requirePositive(rule.getTransactionCountThreshold(), "transactionCountThreshold");
			long count = transactionRepository.countByAccountIdAndTimestampBetween(accountId, start, end);
			boolean triggered = evaluateComparison(BigDecimal.valueOf(count), BigDecimal.valueOf(countThreshold), operator);
			return new EvaluationPayload(triggered, "dailyCount=" + count + " for accountId=" + accountId);
		}

		BigDecimal threshold = requireThreshold(rule);
		BigDecimal totalAmountUsd = transactionRepository
			.sumAmountUsdByAccountIdAndTimestampBetween(accountId, start, end)
			.setScale(4, RoundingMode.HALF_UP);
		boolean triggered = evaluateComparison(totalAmountUsd, threshold, operator);
		return new EvaluationPayload(triggered, "dailyAmountUsd=" + totalAmountUsd + " for accountId=" + accountId);
	}

	private EvaluationPayload evaluateNewPayeeRule(Rule rule, Transaction transaction) {
		Long accountId = requireAccountId(transaction);
		Long payeeId = transaction.getPayeeId();
		if (payeeId == null) {
			throw new IllegalArgumentException("NEW_PAYEE rule requires transaction payeeId");
		}
		LocalDateTime referenceTime = requireTransactionTimestamp(transaction);
		boolean existedBefore = transactionRepository.existsByAccountIdAndPayeeIdAndTimestampBefore(accountId, payeeId, referenceTime);
		boolean triggered = !existedBefore;
		return new EvaluationPayload(triggered, "payeeId=" + payeeId + " is " + (triggered ? "new" : "existing") + " for accountId=" + accountId);
	}

	private EvaluationPayload evaluateCustomRule(Rule rule, Transaction transaction) {
		String metric = normalizedValue(rule.getMetric());
		if ("AMOUNT".equals(metric)) {
			return evaluateAmountThresholdRule(rule, transaction);
		}
		if ("TRANSACTION_COUNT".equals(metric)) {
			return evaluateCountInTimeWindowRule(rule, transaction);
		}
		throw new IllegalArgumentException("Unsupported custom rule configuration. Provide a supported ruleType or metric.");
	}

	private void normalizeRule(Rule rule) {
		rule.setRuleType(normalizedValue(rule.getRuleType()));
		rule.setMetric(normalizedValue(rule.getMetric()));
		rule.setComparisonOperator(normalizedValue(rule.getComparisonOperator()));
		rule.setTimeWindowUnit(normalizedValue(rule.getTimeWindowUnit()));
		rule.setPayeeScope(normalizedValue(rule.getPayeeScope()));
		rule.setSeverity(normalizedValue(rule.getSeverity()));
	}

	private void validateRule(Rule rule) {
		if (rule.getRuleType() == null || rule.getRuleType().isBlank()) {
			throw new IllegalArgumentException("ruleType is required");
		}

		switch (rule.getRuleType()) {
			case "AMOUNT_THRESHOLD" -> requireThreshold(rule);
			case "AMOUNT_RANGE" -> {
				if (rule.getMinimumThreshold() == null || rule.getMaximumThreshold() == null) {
					throw new IllegalArgumentException("AMOUNT_RANGE requires minimumThreshold and maximumThreshold");
				}
				if (rule.getMinimumThreshold().compareTo(rule.getMaximumThreshold()) > 0) {
					throw new IllegalArgumentException("minimumThreshold cannot be greater than maximumThreshold");
				}
			}
			case "COUNT_IN_TIME_WINDOW" -> {
				requirePositive(rule.getTransactionCountThreshold(), "transactionCountThreshold");
				requirePositive(rule.getTimeWindowValue(), "timeWindowValue");
				requireTimeWindowUnit(rule.getTimeWindowUnit());
			}
			case "DAILY_LIMIT" -> {
				String metric = normalizedValue(rule.getMetric());
				if ("TRANSACTION_COUNT".equals(metric)) {
					requirePositive(rule.getTransactionCountThreshold(), "transactionCountThreshold");
				} else {
					requireThreshold(rule);
				}
			}
			case "NEW_PAYEE" -> {
				if (rule.getPayeeScope() == null || rule.getPayeeScope().isBlank()) {
					rule.setPayeeScope("NEW_PAYEE_ONLY");
				}
			}
			default -> {
				if (rule.getMetric() == null || rule.getMetric().isBlank()) {
					throw new IllegalArgumentException("Custom rule types require metric");
				}
			}
		}
	}

	private BigDecimal requireTransactionAmountUsd(Transaction transaction) {
		if (transaction.getAmountUsd() == null) {
			throw new IllegalArgumentException("Transaction amountUsd is missing. Recreate transaction after currency setup.");
		}
		return transaction.getAmountUsd();
	}

	private Transaction getTransactionById(Long transactionId) {
		return transactionRepository.findById(transactionId)
			.orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));
	}

	private BigDecimal requireThreshold(Rule rule) {
		if (rule.getThreshold() == null) {
			throw new IllegalArgumentException("threshold is required");
		}
		return rule.getThreshold();
	}

	private Long requireAccountId(Transaction transaction) {
		if (transaction.getAccountId() == null) {
			throw new IllegalArgumentException("Transaction accountId is required for rule evaluation");
		}
		return transaction.getAccountId();
	}

	private LocalDateTime requireTransactionTimestamp(Transaction transaction) {
		if (transaction.getTimestamp() == null) {
			throw new IllegalArgumentException("Transaction timestamp is required for time-based rule evaluation");
		}
		return transaction.getTimestamp();
	}

	private int requirePositive(Integer value, String fieldName) {
		if (value == null || value <= 0) {
			throw new IllegalArgumentException(fieldName + " must be greater than zero");
		}
		return value;
	}

	private String requireTimeWindowUnit(String unit) {
		String normalized = normalizedValue(unit);
		if (normalized == null || normalized.isBlank()) {
			throw new IllegalArgumentException("timeWindowUnit is required");
		}
		if (!"MINUTE".equals(normalized) && !"HOUR".equals(normalized) && !"DAY".equals(normalized)) {
			throw new IllegalArgumentException("Unsupported timeWindowUnit: " + unit + ". Use MINUTE, HOUR, or DAY.");
		}
		return normalized;
	}

	private LocalDateTime subtractWindow(LocalDateTime referenceTime, int windowValue, String windowUnit) {
		return switch (windowUnit) {
			case "MINUTE" -> referenceTime.minusMinutes(windowValue);
			case "HOUR" -> referenceTime.minusHours(windowValue);
			case "DAY" -> referenceTime.minusDays(windowValue);
			default -> throw new IllegalArgumentException("Unsupported timeWindowUnit: " + windowUnit);
		};
	}

	private String normalizeOperator(String operator) {
		if (operator == null || operator.isBlank()) {
			return "GREATER_THAN";
		}
		String normalized = operator.trim().toUpperCase(Locale.ROOT);
		return switch (normalized) {
			case "GREATHER_THAN" -> "GREATER_THAN";
			case "GREATHER_THAN_OR_EQUAL", "GREATHER_THAN_EQUAL" -> "GREATER_THAN_OR_EQUAL";
			case "GT" -> "GREATER_THAN";
			case "GTE" -> "GREATER_THAN_OR_EQUAL";
			case "LT" -> "LESS_THAN";
			case "LTE" -> "LESS_THAN_OR_EQUAL";
			case "EQ", "EQUAL_TO" -> "EQUAL";
			default -> normalized;
		};
	}

	private String normalizedValue(String value) {
		if (value == null) {
			return null;
		}
		String normalized = value.trim();
		if (normalized.isEmpty()) {
			return normalized;
		}
		return normalized.toUpperCase(Locale.ROOT);
	}

	private boolean getBooleanParameter(Map<String, Object> parameters, String key, boolean defaultValue) {
		if (parameters == null || !parameters.containsKey(key) || parameters.get(key) == null) {
			return defaultValue;
		}
		Object value = parameters.get(key);
		if (value instanceof Boolean booleanValue) {
			return booleanValue;
		}
		return Boolean.parseBoolean(String.valueOf(value));
	}

	private boolean evaluateComparison(BigDecimal leftValue, BigDecimal rightValue, String operator) {
		int comparison = leftValue.compareTo(rightValue);
		return switch (operator) {
			case "GREATER_THAN" -> comparison > 0;
			case "GREATER_THAN_OR_EQUAL" -> comparison >= 0;
			case "LESS_THAN" -> comparison < 0;
			case "LESS_THAN_OR_EQUAL" -> comparison <= 0;
			case "EQUAL" -> comparison == 0;
			default -> throw new IllegalArgumentException("Unsupported comparison operator: " + operator);
		};
	}

	private record EvaluationPayload(boolean triggered, String message) {
	}
}