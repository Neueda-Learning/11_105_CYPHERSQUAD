package com.CypherSquad.backend.controllers;

import com.CypherSquad.backend.models.Rule;
import com.CypherSquad.backend.models.RuleEvaluationResult;
import com.CypherSquad.backend.services.RuleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RuleControllerTest {

	@Mock
	private RuleService ruleService;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new RuleController(ruleService)).build();
		objectMapper = new ObjectMapper().findAndRegisterModules();
	}

	@Test
	void getRulesReturnsList() throws Exception {
		when(ruleService.getRules()).thenReturn(List.of(sampleRule(1L), sampleRule(2L)));

		mockMvc.perform(get("/rules"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].ruleId").value(1L));

		verify(ruleService).getRules();
	}

	@Test
	void createRuleReturnsCreated() throws Exception {
		when(ruleService.createRule(any(Rule.class))).thenReturn(sampleRule(3L));

		mockMvc.perform(post("/rules")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(sampleRule(null))))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.ruleId").value(3L));

		verify(ruleService).createRule(any(Rule.class));
	}

	@Test
	void updateRuleReturnsUpdatedRule() throws Exception {
		when(ruleService.updateRule(any(Long.class), any(Rule.class))).thenReturn(sampleRule(4L));

		mockMvc.perform(put("/rules/4")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(sampleRule(null))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ruleId").value(4L));

		verify(ruleService).updateRule(any(Long.class), any(Rule.class));
	}

	@Test
	void getRuleByIdReturnsRule() throws Exception {
		when(ruleService.getRuleById(5L)).thenReturn(sampleRule(5L));

		mockMvc.perform(get("/rules/5"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ruleId").value(5L));

		verify(ruleService).getRuleById(5L);
	}

	@Test
	void deleteRuleReturnsNoContent() throws Exception {
		mockMvc.perform(delete("/rules/6"))
			.andExpect(status().isNoContent());

		verify(ruleService).deleteRule(6L);
	}

	@Test
	void evaluateRuleForTransactionReturnsResult() throws Exception {
		when(ruleService.evaluateRuleForTransaction(7L, 70L)).thenReturn(sampleResult(7L, 70L, true));

		mockMvc.perform(post("/rules/7/evaluate/transactions/70"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ruleId").value(7L))
			.andExpect(jsonPath("$.triggered").value(true));

		verify(ruleService).evaluateRuleForTransaction(7L, 70L);
	}

	@Test
	void evaluateActiveRulesForTransactionReturnsResults() throws Exception {
		when(ruleService.evaluateActiveRulesForTransaction(99L)).thenReturn(
			List.of(sampleResult(1L, 99L, false), sampleResult(2L, 99L, true))
		);

		mockMvc.perform(post("/rules/evaluate/transactions/99"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].transactionId").value(99L))
			.andExpect(jsonPath("$[1].triggered").value(true));

		verify(ruleService).evaluateActiveRulesForTransaction(99L);
	}

	private Rule sampleRule(Long id) {
		Rule rule = new Rule();
		rule.setRuleId(id);
		rule.setRuleName("Large transfer");
		rule.setRuleType("AMOUNT_THRESHOLD");
		rule.setComparisonOperator("GREATER_THAN");
		rule.setThreshold(new BigDecimal("5000.00"));
		rule.setSeverity("HIGH");
		rule.setActive(true);
		return rule;
	}

	private RuleEvaluationResult sampleResult(Long ruleId, Long transactionId, boolean triggered) {
		RuleEvaluationResult result = new RuleEvaluationResult();
		result.setRuleId(ruleId);
		result.setTransactionId(transactionId);
		result.setRuleType("AMOUNT_THRESHOLD");
		result.setComparisonOperator("GREATER_THAN");
		result.setThreshold(new BigDecimal("5000.00"));
		result.setTransactionAmountUsd(new BigDecimal("7000.00"));
		result.setTriggered(triggered);
		result.setAlertId(triggered ? 100L : null);
		result.setMessage(triggered ? "triggered" : "not triggered");
		return result;
	}
}
