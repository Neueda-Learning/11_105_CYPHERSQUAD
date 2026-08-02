package com.CypherSquad.backend.repositories;

import com.CypherSquad.backend.models.Rule;

import java.util.List;
import java.util.Optional;

public interface RuleRepository {
	Rule save(Rule rule);

	List<Rule> findAll();

	Optional<Rule> findById(Long ruleId);

	void deleteById(Long ruleId);
}