package com.CypherSquad.backend.controllers;

import com.CypherSquad.backend.models.Rule;
import com.CypherSquad.backend.services.RuleService;
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
	public ResponseEntity<Rule> createRule(@RequestBody Rule rule) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ruleService.createRule(rule));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Rule> updateRule(@PathVariable("id") Long ruleId, @RequestBody Rule rule) {
		return ResponseEntity.ok(ruleService.updateRule(ruleId, rule));
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
}