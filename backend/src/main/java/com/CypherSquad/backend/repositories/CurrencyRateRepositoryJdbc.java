package com.CypherSquad.backend.repositories;

import com.CypherSquad.backend.models.CurrencyRate;
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
public class CurrencyRateRepositoryJdbc implements CurrencyRateRepository {
	private static final RowMapper<CurrencyRate> CURRENCY_RATE_ROW_MAPPER = (rs, rowNum) -> {
		CurrencyRate currencyRate = new CurrencyRate();
		currencyRate.setCurrencyId(rs.getLong("currency_id"));
		currencyRate.setCurrencyCode(rs.getString("currency_code"));
		currencyRate.setCurrencyName(rs.getString("currency_name"));
		currencyRate.setUsdRate(rs.getBigDecimal("usd_rate"));
		currencyRate.setActive(rs.getBoolean("active"));
		return currencyRate;
	};

	private final JdbcTemplate jdbcTemplate;

	public CurrencyRateRepositoryJdbc(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public CurrencyRate save(CurrencyRate currencyRate) {
		if (currencyRate.getCurrencyId() == null) {
			return insert(currencyRate);
		}
		return update(currencyRate);
	}

	private CurrencyRate insert(CurrencyRate currencyRate) {
		String sql = "INSERT INTO currency_rates (currency_code, currency_name, usd_rate, active) VALUES (?, ?, ?, ?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, currencyRate.getCurrencyCode());
			ps.setString(2, currencyRate.getCurrencyName());
			ps.setBigDecimal(3, currencyRate.getUsdRate());
			ps.setBoolean(4, currencyRate.isActive());
			return ps;
		}, keyHolder);

		Number key = keyHolder.getKey();
		if (key != null) {
			currencyRate.setCurrencyId(key.longValue());
		}
		return currencyRate;
	}

	private CurrencyRate update(CurrencyRate currencyRate) {
		String sql = "UPDATE currency_rates SET currency_code = ?, currency_name = ?, usd_rate = ?, active = ? WHERE currency_id = ?";
		jdbcTemplate.update(sql,
			currencyRate.getCurrencyCode(),
			currencyRate.getCurrencyName(),
			currencyRate.getUsdRate(),
			currencyRate.isActive(),
			currencyRate.getCurrencyId());
		return currencyRate;
	}

	@Override
	public List<CurrencyRate> findAll() {
		String sql = "SELECT currency_id, currency_code, currency_name, usd_rate, active FROM currency_rates ORDER BY currency_id";
		return jdbcTemplate.query(sql, CURRENCY_RATE_ROW_MAPPER);
	}

	@Override
	public Optional<CurrencyRate> findById(Long currencyId) {
		String sql = "SELECT currency_id, currency_code, currency_name, usd_rate, active FROM currency_rates WHERE currency_id = ?";
		List<CurrencyRate> currencies = jdbcTemplate.query(sql, CURRENCY_RATE_ROW_MAPPER, currencyId);
		return currencies.stream().findFirst();
	}

	@Override
	public void deleteById(Long currencyId) {
		jdbcTemplate.update("DELETE FROM currency_rates WHERE currency_id = ?", currencyId);
	}

	@Override
	public Optional<CurrencyRate> findByCurrencyCodeIgnoreCase(String currencyCode) {
		String sql = "SELECT currency_id, currency_code, currency_name, usd_rate, active FROM currency_rates WHERE LOWER(currency_code) = LOWER(?)";
		List<CurrencyRate> currencies = jdbcTemplate.query(sql, CURRENCY_RATE_ROW_MAPPER, currencyCode);
		return currencies.stream().findFirst();
	}
}
