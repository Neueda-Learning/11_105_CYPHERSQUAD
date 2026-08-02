package com.CypherSquad.backend.repositories;

import com.CypherSquad.backend.models.Rule;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryRuleRepository implements RuleRepository {
	private final ConcurrentMap<Long, Rule> storage = new ConcurrentHashMap<>();
	private final AtomicLong sequence = new AtomicLong(0);

	@Override
	public Rule save(Rule rule) {
		Long ruleId = rule.getRuleId();
		if (ruleId == null) {
			ruleId = sequence.incrementAndGet();
			rule.setRuleId(ruleId);
		} else {
			Long resolvedRuleId = ruleId;
			sequence.updateAndGet(current -> Math.max(current, resolvedRuleId));
		}
		storage.put(ruleId, rule);
		return rule;
	}

	@Override
	public List<Rule> findAll() {
		List<Rule> rules = new ArrayList<>(storage.values());
		rules.sort(Comparator.comparing(Rule::getRuleId));
		return rules;
	}

	@Override
	public Optional<Rule> findById(Long ruleId) {
		return Optional.ofNullable(storage.get(ruleId));
	}

	@Override
	public void deleteById(Long ruleId) {
		storage.remove(ruleId);
	}
}