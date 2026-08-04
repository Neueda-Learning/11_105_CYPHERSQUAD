package com.CypherSquad.backend.services;

import com.CypherSquad.backend.models.Alert;
import com.CypherSquad.backend.models.Rule;
import com.CypherSquad.backend.models.RuleEvaluationResult;
import com.CypherSquad.backend.models.Transaction;
import com.CypherSquad.backend.repositories.RuleRepository;
import com.CypherSquad.backend.repositories.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleServiceImplTest {

	@Mock
	private RuleRepository ruleRepository;

	@Mock
	private AlertService alertService;

	@Mock
	private TransactionRepository transactionRepository;

	@InjectMocks
	private RuleServiceImpl ruleService;

	@Test
	void createRuleNormalizesAndSaves() {
		Rule input = amountThresholdRule(null, " amount_threshold ", " greater_than ", " high ", "5000.00");
		when(ruleRepository.save(any(Rule.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Rule saved = ruleService.createRule(input);

		assertEquals("AMOUNT_THRESHOLD", saved.getRuleType());
		assertEquals("GREATER_THAN", saved.getComparisonOperator());
		assertEquals("HIGH", saved.getSeverity());
	}

	@Test
	void getRulesReturnsRepositoryData() {
		when(ruleRepository.findAll()).thenReturn(List.of(amountThresholdRule(1L, "AMOUNT_THRESHOLD", "GREATER_THAN", "HIGH", "100")));

		assertEquals(1, ruleService.getRules().size());
	}

	@Test
	void getRuleByIdThrowsWhenMissing() {
		when(ruleRepository.findById(30L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> ruleService.getRuleById(30L));
	}

	@Test
	void updateRuleSetsExistingIdBeforeSave() {
		Rule existing = amountThresholdRule(8L, "AMOUNT_THRESHOLD", "GREATER_THAN", "MEDIUM", "100");
		Rule incoming = amountThresholdRule(null, "amount_threshold", "greater_than", "high", "200");
		when(ruleRepository.findById(8L)).thenReturn(Optional.of(existing));
		when(ruleRepository.save(any(Rule.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Rule updated = ruleService.updateRule(8L, incoming);

		assertEquals(8L, updated.getRuleId());
		assertEquals("AMOUNT_THRESHOLD", updated.getRuleType());
		assertEquals(new BigDecimal("200"), updated.getThreshold());
	}

	@Test
	void deleteRuleThrowsWhenMissing() {
		when(ruleRepository.findById(9L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> ruleService.deleteRule(9L));
		verify(ruleRepository, never()).deleteById(9L);
	}

	@Test
	void deleteRuleCallsRepositoryWhenPresent() {
		when(ruleRepository.findById(10L)).thenReturn(Optional.of(amountThresholdRule(10L, "AMOUNT_THRESHOLD", "GREATER_THAN", "HIGH", "100")));

		ruleService.deleteRule(10L);

		verify(ruleRepository).deleteById(10L);
	}

	@Test
	void evaluateRuleForTransactionCreatesAlertWhenTriggered() {
		Rule rule = amountThresholdRule(11L, "AMOUNT_THRESHOLD", "GREATER_THAN", "HIGH", "500.00");
		Transaction tx = sampleTransaction(101L, new BigDecimal("700.00"));
		when(ruleRepository.findById(11L)).thenReturn(Optional.of(rule));
		when(transactionRepository.findById(101L)).thenReturn(Optional.of(tx));
		Alert createdAlert = new Alert();
		createdAlert.setAlertId(900L);
		when(alertService.createAlert(any(Alert.class))).thenReturn(createdAlert);

		RuleEvaluationResult result = ruleService.evaluateRuleForTransaction(11L, 101L);

		assertTrue(result.isTriggered());
		assertEquals(900L, result.getAlertId());
		assertTrue(result.getMessage().contains("Rule triggered and alert created"));

		ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
		verify(alertService).createAlert(alertCaptor.capture());
		assertEquals(101L, alertCaptor.getValue().getTransactionId());
		assertEquals(11L, alertCaptor.getValue().getRuleId());
		assertEquals("HIGH", alertCaptor.getValue().getSeverity());
	}

	@Test
	void evaluateRuleForTransactionReturnsNotTriggeredWithoutAlert() {
		Rule rule = amountThresholdRule(12L, "AMOUNT_THRESHOLD", "GREATER_THAN", "LOW", "900.00");
		Transaction tx = sampleTransaction(102L, new BigDecimal("700.00"));
		when(ruleRepository.findById(12L)).thenReturn(Optional.of(rule));
		when(transactionRepository.findById(102L)).thenReturn(Optional.of(tx));

		RuleEvaluationResult result = ruleService.evaluateRuleForTransaction(12L, 102L);

		assertFalse(result.isTriggered());
		assertEquals(null, result.getAlertId());
		assertTrue(result.getMessage().contains("Rule not triggered"));
		verify(alertService, never()).createAlert(any(Alert.class));
	}

	@Test
	void evaluateActiveRulesForTransactionEvaluatesAllActiveRules() {
		Transaction tx = sampleTransaction(103L, new BigDecimal("600.00"));
		Rule first = amountThresholdRule(20L, "AMOUNT_THRESHOLD", "GREATER_THAN", "MEDIUM", "100.00");
		Rule second = amountThresholdRule(21L, "AMOUNT_THRESHOLD", "GREATER_THAN", "MEDIUM", "800.00");
		when(transactionRepository.findById(103L)).thenReturn(Optional.of(tx));
		when(ruleRepository.findByActiveTrue()).thenReturn(List.of(first, second));
		Alert createdAlert = new Alert();
		createdAlert.setAlertId(1001L);
		when(alertService.createAlert(any(Alert.class))).thenReturn(createdAlert);

		List<RuleEvaluationResult> results = ruleService.evaluateActiveRulesForTransaction(103L);

		assertEquals(2, results.size());
		assertTrue(results.get(0).isTriggered());
		assertFalse(results.get(1).isTriggered());
		assertNotNull(results.get(0).getAlertId());
	}

	@Test
	void evaluateRuleForTransactionThrowsWhenTransactionMissing() {
		Rule rule = amountThresholdRule(13L, "AMOUNT_THRESHOLD", "GREATER_THAN", "HIGH", "500.00");
		when(ruleRepository.findById(13L)).thenReturn(Optional.of(rule));
		when(transactionRepository.findById(404L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> ruleService.evaluateRuleForTransaction(13L, 404L));
	}

	private Rule amountThresholdRule(Long id, String ruleType, String operator, String severity, String threshold) {
		Rule rule = new Rule();
		rule.setRuleId(id);
		rule.setRuleName("Threshold Rule");
		rule.setRuleType(ruleType);
		rule.setComparisonOperator(operator);
		rule.setThreshold(new BigDecimal(threshold));
		rule.setSeverity(severity);
		rule.setActive(true);
		return rule;
	}

	private Transaction sampleTransaction(Long id, BigDecimal amountUsd) {
		Transaction transaction = new Transaction();
		transaction.setTransactionId(id);
		transaction.setAccountId(222L);
		transaction.setAmount(new BigDecimal("100.00"));
		transaction.setAmountUsd(amountUsd);
		transaction.setCurrency("USD");
		transaction.setTimestamp(LocalDateTime.of(2026, 1, 1, 9, 0));
		return transaction;
	}
}
