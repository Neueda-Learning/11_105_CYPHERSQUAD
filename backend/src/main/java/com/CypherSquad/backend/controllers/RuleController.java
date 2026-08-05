package com.CypherSquad.backend.controllers;

import com.CypherSquad.backend.dto.RuleRequest;
import com.CypherSquad.backend.models.Rule;
import com.CypherSquad.backend.models.RuleEvaluationResult;
import com.CypherSquad.backend.services.RuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rules")
public class RuleController {
	private final RuleService ruleService;

	public RuleController(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	@GetMapping
	public ResponseEntity<List<Rule>> getRules() {
		return ResponseEntity.ok(ruleService.getRules());
	}

	@PostMapping
	public ResponseEntity<Rule> createRule(@Valid @RequestBody RuleRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ruleService.createRule(toRule(request)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Rule> updateRule(@PathVariable("id") Long ruleId, @Valid @RequestBody RuleRequest request) {
		return ResponseEntity.ok(ruleService.updateRule(ruleId, toRule(request)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Rule> getRuleById(@PathVariable("id") Long ruleId) {
		return ResponseEntity.ok(ruleService.getRuleById(ruleId));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteRule(@PathVariable("id") Long ruleId) {
		ruleService.deleteRule(ruleId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/evaluate/transactions/{transactionId}")
	public ResponseEntity<RuleEvaluationResult> evaluateRuleForTransaction(@PathVariable("id") Long ruleId,
		@PathVariable Long transactionId) {
		return ResponseEntity.ok(ruleService.evaluateRuleForTransaction(ruleId, transactionId));
	}

	@PostMapping("/evaluate/transactions/{transactionId}")
	public ResponseEntity<List<RuleEvaluationResult>> evaluateActiveRulesForTransaction(@PathVariable Long transactionId) {
		return ResponseEntity.ok(ruleService.evaluateActiveRulesForTransaction(transactionId));
	}

	private Rule toRule(RuleRequest request) {
		Rule rule = new Rule();
		rule.setRuleName(request.getRuleName());
		rule.setRuleType(request.getRuleType());
		rule.setMetric(request.getMetric());
		rule.setComparisonOperator(request.getComparisonOperator());
		rule.setThreshold(request.getThreshold());
		rule.setMinimumThreshold(request.getMinimumThreshold());
		rule.setMaximumThreshold(request.getMaximumThreshold());
		rule.setTransactionCountThreshold(request.getTransactionCountThreshold());
		rule.setTimeWindowValue(request.getTimeWindowValue());
		rule.setTimeWindowUnit(request.getTimeWindowUnit());
		rule.setPayeeScope(request.getPayeeScope());
		rule.setSeverity(request.getSeverity());
		rule.setActive(request.getActive());
		rule.setParameters(request.getParameters());
		return rule;
	}
}