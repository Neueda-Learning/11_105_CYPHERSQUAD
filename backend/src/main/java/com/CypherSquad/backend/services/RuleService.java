package com.CypherSquad.backend.services;

import com.CypherSquad.backend.models.Rule;
import com.CypherSquad.backend.models.RuleEvaluationResult;

import java.util.List;

public interface RuleService {
	Rule createRule(Rule rule);

	List<Rule> getRules();

	Rule getRuleById(Long ruleId);

	Rule updateRule(Long ruleId, Rule rule);

	void deleteRule(Long ruleId);

	RuleEvaluationResult evaluateRuleForTransaction(Long ruleId, Long transactionId);

	List<RuleEvaluationResult> evaluateActiveRulesForTransaction(Long transactionId);
}