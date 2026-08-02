package com.CypherSquad.backend.services;

import com.CypherSquad.backend.models.Rule;
import com.CypherSquad.backend.repositories.RuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleServiceImpl implements RuleService {
	private final RuleRepository ruleRepository;

	public RuleServiceImpl(RuleRepository ruleRepository) {
		this.ruleRepository = ruleRepository;
	}

	@Override
	public Rule createRule(Rule rule) {
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
		return ruleRepository.save(rule);
	}

	@Override
	public void deleteRule(Long ruleId) {
		if (ruleRepository.findById(ruleId).isEmpty()) {
			throw new ResourceNotFoundException("Rule not found: " + ruleId);
		}
		ruleRepository.deleteById(ruleId);
	}
}