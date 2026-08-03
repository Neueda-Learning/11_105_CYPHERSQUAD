package com.CypherSquad.backend.repositories;

import com.CypherSquad.backend.models.Rule;
import com.CypherSquad.backend.models.RuleParametersConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class RuleRepositoryJdbc implements RuleRepository {
	private static final RuleParametersConverter PARAMETERS_CONVERTER = new RuleParametersConverter();

	private static final RowMapper<Rule> RULE_ROW_MAPPER = (rs, rowNum) -> {
		Rule rule = new Rule();
		rule.setRuleId(rs.getLong("rule_id"));
		rule.setRuleName(rs.getString("rule_name"));
		rule.setRuleType(rs.getString("rule_type"));
		rule.setMetric(rs.getString("metric"));
		rule.setComparisonOperator(rs.getString("comparison_operator"));
		rule.setThreshold(rs.getBigDecimal("threshold"));
		rule.setMinimumThreshold(rs.getBigDecimal("minimum_threshold"));
		rule.setMaximumThreshold(rs.getBigDecimal("maximum_threshold"));
		rule.setTransactionCountThreshold(rs.getObject("transaction_count_threshold", Integer.class));
		rule.setTimeWindowValue(rs.getObject("time_window_value", Integer.class));
		rule.setTimeWindowUnit(rs.getString("time_window_unit"));
		rule.setPayeeScope(rs.getString("payee_scope"));
		rule.setSeverity(rs.getString("severity"));
		rule.setActive(rs.getBoolean("active"));
		rule.setParameters(PARAMETERS_CONVERTER.convertToEntityAttribute(rs.getString("parameters_json")));
		return rule;
	};

	private final JdbcTemplate jdbcTemplate;

	public RuleRepositoryJdbc(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Rule save(Rule rule) {
		if (rule.getRuleId() == null) {
			return insert(rule);
		}
		return update(rule);
	}

	private Rule insert(Rule rule) {
		String sql = "INSERT INTO rules (rule_name, rule_type, metric, comparison_operator, threshold, minimum_threshold, maximum_threshold, transaction_count_threshold, time_window_value, time_window_unit, payee_scope, severity, active, parameters_json) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			bindRuleFields(ps, rule);
			return ps;
		}, keyHolder);

		Number key = keyHolder.getKey();
		if (key != null) {
			rule.setRuleId(key.longValue());
		}
		return rule;
	}

	private Rule update(Rule rule) {
		String sql = "UPDATE rules SET rule_name = ?, rule_type = ?, metric = ?, comparison_operator = ?, threshold = ?, minimum_threshold = ?, maximum_threshold = ?, transaction_count_threshold = ?, time_window_value = ?, time_window_unit = ?, payee_scope = ?, severity = ?, active = ?, parameters_json = ? WHERE rule_id = ?";
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql);
			bindRuleFields(ps, rule);
			ps.setLong(15, rule.getRuleId());
			return ps;
		});
		return rule;
	}

	private void bindRuleFields(PreparedStatement ps, Rule rule) throws java.sql.SQLException {
		ps.setString(1, rule.getRuleName());
		ps.setString(2, rule.getRuleType());
		ps.setString(3, rule.getMetric());
		ps.setString(4, rule.getComparisonOperator());
		ps.setBigDecimal(5, rule.getThreshold());
		ps.setBigDecimal(6, rule.getMinimumThreshold());
		ps.setBigDecimal(7, rule.getMaximumThreshold());
		ps.setObject(8, rule.getTransactionCountThreshold());
		ps.setObject(9, rule.getTimeWindowValue());
		ps.setString(10, rule.getTimeWindowUnit());
		ps.setString(11, rule.getPayeeScope());
		ps.setString(12, rule.getSeverity());
		ps.setBoolean(13, rule.isActive());
		ps.setString(14, PARAMETERS_CONVERTER.convertToDatabaseColumn(rule.getParameters()));
	}

	@Override
	public List<Rule> findAll() {
		String sql = "SELECT rule_id, rule_name, rule_type, metric, comparison_operator, threshold, minimum_threshold, maximum_threshold, transaction_count_threshold, time_window_value, time_window_unit, payee_scope, severity, active, parameters_json FROM rules ORDER BY rule_id";
		return jdbcTemplate.query(sql, RULE_ROW_MAPPER);
	}

	@Override
	public Optional<Rule> findById(Long ruleId) {
		String sql = "SELECT rule_id, rule_name, rule_type, metric, comparison_operator, threshold, minimum_threshold, maximum_threshold, transaction_count_threshold, time_window_value, time_window_unit, payee_scope, severity, active, parameters_json FROM rules WHERE rule_id = ?";
		List<Rule> rules = jdbcTemplate.query(sql, RULE_ROW_MAPPER, ruleId);
		return rules.stream().findFirst();
	}

	@Override
	public void deleteById(Long ruleId) {
		jdbcTemplate.update("DELETE FROM rules WHERE rule_id = ?", ruleId);
	}

	@Override
	public List<Rule> findByActiveTrue() {
		String sql = "SELECT rule_id, rule_name, rule_type, metric, comparison_operator, threshold, minimum_threshold, maximum_threshold, transaction_count_threshold, time_window_value, time_window_unit, payee_scope, severity, active, parameters_json FROM rules WHERE active = ? ORDER BY rule_id";
		return jdbcTemplate.query(sql, RULE_ROW_MAPPER, true);
	}
}
