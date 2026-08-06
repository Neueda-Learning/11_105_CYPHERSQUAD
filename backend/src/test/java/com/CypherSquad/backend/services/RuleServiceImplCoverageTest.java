package com.CypherSquad.backend.services;

import com.CypherSquad.backend.models.Rule;
import com.CypherSquad.backend.models.RuleEvaluationResult;
import com.CypherSquad.backend.models.Alert;
import com.CypherSquad.backend.models.Transaction;
import com.CypherSquad.backend.repositories.RuleRepository;
import com.CypherSquad.backend.repositories.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleServiceImplCoverageTest {

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private AlertService alertService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private RuleServiceImpl ruleService;

    @Test
    void evaluateAmountRangeRuleSupportsInclusiveAndExclusiveModes() {
        Rule inclusiveRule = baseRule(101L, "AMOUNT_RANGE");
        inclusiveRule.setMinimumThreshold(new BigDecimal("100.00"));
        inclusiveRule.setMaximumThreshold(new BigDecimal("200.00"));
        inclusiveRule.setParameters(new LinkedHashMap<>());
        inclusiveRule.getParameters().put("inclusive", true);

        Rule exclusiveRule = baseRule(102L, "AMOUNT_RANGE");
        exclusiveRule.setMinimumThreshold(new BigDecimal("100.00"));
        exclusiveRule.setMaximumThreshold(new BigDecimal("200.00"));
        exclusiveRule.setParameters(new LinkedHashMap<>());
        exclusiveRule.getParameters().put("inclusive", false);

        Transaction tx = tx(1L, 22L, 33L, new BigDecimal("100.0000"), LocalDateTime.of(2026, 2, 1, 10, 0));

        when(ruleRepository.findById(101L)).thenReturn(Optional.of(inclusiveRule));
        when(ruleRepository.findById(102L)).thenReturn(Optional.of(exclusiveRule));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));
        when(alertService.createAlert(any(Alert.class))).thenReturn(createdAlert(9001L));

        RuleEvaluationResult inclusiveResult = ruleService.evaluateRuleForTransaction(101L, 1L);
        RuleEvaluationResult exclusiveResult = ruleService.evaluateRuleForTransaction(102L, 1L);

        assertTrue(inclusiveResult.isTriggered());
        assertFalse(exclusiveResult.isTriggered());
        verify(alertService, times(1)).createAlert(any(Alert.class));
    }

    @Test
    void evaluateCountInTimeWindowRuleUsesWindowAndThreshold() {
        Rule rule = baseRule(201L, "COUNT_IN_TIME_WINDOW");
        rule.setComparisonOperator("GREATER_THAN_OR_EQUAL");
        rule.setTransactionCountThreshold(3);
        rule.setTimeWindowValue(2);
        rule.setTimeWindowUnit("hour");

        Transaction tx = tx(2L, 200L, 300L, new BigDecimal("50.0000"), LocalDateTime.of(2026, 2, 1, 12, 0));

        when(ruleRepository.findById(201L)).thenReturn(Optional.of(rule));
        when(transactionRepository.findById(2L)).thenReturn(Optional.of(tx));
        when(transactionRepository.countByAccountIdAndTimestampBetween(
            org.mockito.ArgumentMatchers.eq(200L),
            org.mockito.ArgumentMatchers.any(LocalDateTime.class),
            org.mockito.ArgumentMatchers.eq(LocalDateTime.of(2026, 2, 1, 12, 0))
        )).thenReturn(3L);
        when(alertService.createAlert(any(Alert.class))).thenReturn(createdAlert(9002L));

        RuleEvaluationResult result = ruleService.evaluateRuleForTransaction(201L, 2L);

        assertTrue(result.isTriggered());
        assertTrue(result.getMessage().contains("Rule triggered") || result.getMessage().contains("Rule not triggered"));
    }

    @Test
    void evaluateDailyLimitSupportsAmountAndTransactionCountMetrics() {
        Rule amountRule = baseRule(301L, "DAILY_LIMIT");
        amountRule.setMetric("amount");
        amountRule.setComparisonOperator("GT");
        amountRule.setThreshold(new BigDecimal("100.0000"));

        Rule countRule = baseRule(302L, "DAILY_LIMIT");
        countRule.setMetric("transaction_count");
        countRule.setComparisonOperator("GTE");
        countRule.setTransactionCountThreshold(4);

        Transaction tx = tx(3L, 900L, 901L, new BigDecimal("10.0000"), LocalDateTime.of(2026, 2, 5, 7, 30));

        when(ruleRepository.findById(301L)).thenReturn(Optional.of(amountRule));
        when(ruleRepository.findById(302L)).thenReturn(Optional.of(countRule));
        when(transactionRepository.findById(3L)).thenReturn(Optional.of(tx));
        when(transactionRepository.sumAmountUsdByAccountIdAndTimestampBetween(
            org.mockito.ArgumentMatchers.eq(900L),
            org.mockito.ArgumentMatchers.any(LocalDateTime.class),
            org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        )).thenReturn(new BigDecimal("120.0000"));
        when(transactionRepository.countByAccountIdAndTimestampBetween(
            org.mockito.ArgumentMatchers.eq(900L),
            org.mockito.ArgumentMatchers.any(LocalDateTime.class),
            org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        )).thenReturn(4L);
        when(alertService.createAlert(any(Alert.class))).thenReturn(createdAlert(9003L));

        RuleEvaluationResult amountResult = ruleService.evaluateRuleForTransaction(301L, 3L);
        RuleEvaluationResult countResult = ruleService.evaluateRuleForTransaction(302L, 3L);

        assertTrue(amountResult.isTriggered());
        assertTrue(countResult.isTriggered());
    }

    @Test
    void evaluateNewPayeeRuleTriggersOnlyWhenNoPriorPayment() {
        Rule rule = baseRule(401L, "NEW_PAYEE");
        Transaction tx = tx(4L, 777L, 888L, new BigDecimal("20.0000"), LocalDateTime.of(2026, 3, 1, 8, 0));

        when(ruleRepository.findById(401L)).thenReturn(Optional.of(rule));
        when(transactionRepository.findById(4L)).thenReturn(Optional.of(tx));
        when(transactionRepository.existsByAccountIdAndPayeeIdAndTimestampBefore(777L, 888L, tx.getTimestamp())).thenReturn(false);
        when(alertService.createAlert(any(Alert.class))).thenReturn(createdAlert(9004L));

        RuleEvaluationResult first = ruleService.evaluateRuleForTransaction(401L, 4L);
        assertTrue(first.isTriggered());

        when(transactionRepository.existsByAccountIdAndPayeeIdAndTimestampBefore(777L, 888L, tx.getTimestamp())).thenReturn(true);
        RuleEvaluationResult second = ruleService.evaluateRuleForTransaction(401L, 4L);
        assertFalse(second.isTriggered());
    }

    @Test
    void evaluateCustomMetricTransactionCountPathWorks() {
        Rule custom = baseRule(501L, "custom_type");
        custom.setMetric("transaction_count");
        custom.setComparisonOperator("LT");
        custom.setTransactionCountThreshold(5);
        custom.setTimeWindowValue(1);
        custom.setTimeWindowUnit("DAY");

        Transaction tx = tx(5L, 111L, 222L, new BigDecimal("60.0000"), LocalDateTime.of(2026, 3, 2, 9, 0));

        when(ruleRepository.findById(501L)).thenReturn(Optional.of(custom));
        when(transactionRepository.findById(5L)).thenReturn(Optional.of(tx));
        when(transactionRepository.countByAccountIdAndTimestampBetween(
            org.mockito.ArgumentMatchers.eq(111L),
            org.mockito.ArgumentMatchers.any(LocalDateTime.class),
            org.mockito.ArgumentMatchers.eq(tx.getTimestamp())
        )).thenReturn(2L);
        when(alertService.createAlert(any(Alert.class))).thenReturn(createdAlert(9005L));

        RuleEvaluationResult result = ruleService.evaluateRuleForTransaction(501L, 5L);

        assertTrue(result.isTriggered());
        assertEquals("CUSTOM_TYPE", result.getRuleType());
    }

    @Test
    void evaluateRuleThrowsForUnsupportedOperatorAndInvalidRuleShape() {
        Rule badOperator = baseRule(601L, "AMOUNT_THRESHOLD");
        badOperator.setComparisonOperator("something_else");
        badOperator.setThreshold(new BigDecimal("10.00"));

        Rule invalidRange = baseRule(602L, "AMOUNT_RANGE");
        invalidRange.setMinimumThreshold(new BigDecimal("200.00"));
        invalidRange.setMaximumThreshold(new BigDecimal("100.00"));

        Transaction tx = tx(6L, 1L, 2L, new BigDecimal("150.0000"), LocalDateTime.of(2026, 4, 1, 10, 0));

        when(ruleRepository.findById(601L)).thenReturn(Optional.of(badOperator));
        when(ruleRepository.findById(602L)).thenReturn(Optional.of(invalidRange));
        when(transactionRepository.findById(6L)).thenReturn(Optional.of(tx));

        assertThrows(IllegalArgumentException.class, () -> ruleService.evaluateRuleForTransaction(601L, 6L));
        assertThrows(IllegalArgumentException.class, () -> ruleService.evaluateRuleForTransaction(602L, 6L));
    }

    private Rule baseRule(Long id, String ruleType) {
        Rule rule = new Rule();
        rule.setRuleId(id);
        rule.setRuleName("rule-" + id);
        rule.setRuleType(ruleType);
        rule.setComparisonOperator("GREATER_THAN");
        rule.setSeverity("MEDIUM");
        rule.setActive(true);
        rule.setParameters(new LinkedHashMap<>());
        return rule;
    }

    private Transaction tx(Long id, Long accountId, Long payeeId, BigDecimal amountUsd, LocalDateTime timestamp) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(id);
        transaction.setAccountId(accountId);
        transaction.setPayeeId(payeeId);
        transaction.setAmountUsd(amountUsd);
        transaction.setAmount(new BigDecimal("10.0000"));
        transaction.setCurrency("USD");
        transaction.setTimestamp(timestamp);
        return transaction;
    }

    private Alert createdAlert(Long alertId) {
        Alert alert = new Alert();
        alert.setAlertId(alertId);
        return alert;
    }
}
