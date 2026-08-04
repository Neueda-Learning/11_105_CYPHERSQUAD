package com.CypherSquad.backend.repositories;

import com.CypherSquad.backend.models.Transaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class TransactionRepositoryJdbc implements TransactionRepository {
	private static final RowMapper<Transaction> TRANSACTION_ROW_MAPPER = (rs, rowNum) -> {
		Transaction transaction = new Transaction();
		transaction.setTransactionId(rs.getLong("transaction_id"));
		transaction.setAccountId(rs.getLong("account_id"));
		transaction.setPayeeId(rs.getObject("payee_id", Long.class));
		transaction.setAmount(rs.getBigDecimal("amount"));
		transaction.setAmountUsd(rs.getBigDecimal("amount_usd"));
		Timestamp timestamp = rs.getTimestamp("transaction_timestamp");
		transaction.setTimestamp(timestamp == null ? null : timestamp.toLocalDateTime());
		transaction.setCurrency(rs.getString("currency"));
		transaction.setType(rs.getString("type"));
		transaction.setStatus(rs.getString("status"));
		return transaction;
	};

	private final JdbcTemplate jdbcTemplate;

	public TransactionRepositoryJdbc(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Transaction save(Transaction transaction) {
		if (transaction.getTransactionId() == null) {
			return insert(transaction);
		}
		return update(transaction);
	}

	private Transaction insert(Transaction transaction) {
		String sql = "INSERT INTO transactions (account_id, payee_id, amount, amount_usd, transaction_timestamp, currency, type, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setObject(1, transaction.getAccountId());
			ps.setObject(2, transaction.getPayeeId());
			ps.setBigDecimal(3, transaction.getAmount());
			ps.setBigDecimal(4, transaction.getAmountUsd());
			ps.setTimestamp(5, transaction.getTimestamp() == null ? null : Timestamp.valueOf(transaction.getTimestamp()));
			ps.setString(6, transaction.getCurrency());
			ps.setString(7, transaction.getType());
			ps.setString(8, transaction.getStatus());
			return ps;
		}, keyHolder);

		Number key = keyHolder.getKey();
		if (key != null) {
			transaction.setTransactionId(key.longValue());
		}
		return transaction;
	}

	private Transaction update(Transaction transaction) {
		String sql = "UPDATE transactions SET account_id = ?, payee_id = ?, amount = ?, amount_usd = ?, transaction_timestamp = ?, currency = ?, type = ?, status = ? WHERE transaction_id = ?";
		jdbcTemplate.update(sql,
			transaction.getAccountId(),
			transaction.getPayeeId(),
			transaction.getAmount(),
			transaction.getAmountUsd(),
			transaction.getTimestamp() == null ? null : Timestamp.valueOf(transaction.getTimestamp()),
			transaction.getCurrency(),
			transaction.getType(),
			transaction.getStatus(),
			transaction.getTransactionId());
		return transaction;
	}

	@Override
	public List<Transaction> findAll() {
		String sql = "SELECT transaction_id, account_id, payee_id, amount, amount_usd, transaction_timestamp, currency, type, status FROM transactions ORDER BY transaction_id";
		return jdbcTemplate.query(sql, TRANSACTION_ROW_MAPPER);
	}

	@Override
	public Optional<Transaction> findById(Long transactionId) {
		String sql = "SELECT transaction_id, account_id, payee_id, amount, amount_usd, transaction_timestamp, currency, type, status FROM transactions WHERE transaction_id = ?";
		List<Transaction> transactions = jdbcTemplate.query(sql, TRANSACTION_ROW_MAPPER, transactionId);
		return transactions.stream().findFirst();
	}

	@Override
	public long countByAccountIdAndTimestampBetween(Long accountId, LocalDateTime start, LocalDateTime end) {
		String sql = "SELECT COUNT(*) FROM transactions WHERE account_id = ? AND transaction_timestamp BETWEEN ? AND ?";
		Long count = jdbcTemplate.queryForObject(sql, Long.class, accountId, Timestamp.valueOf(start), Timestamp.valueOf(end));
		return count == null ? 0L : count;
	}

	@Override
	public boolean existsByAccountIdAndPayeeIdAndTimestampBefore(Long accountId, Long payeeId, LocalDateTime timestamp) {
		String sql = "SELECT EXISTS(SELECT 1 FROM transactions WHERE account_id = ? AND payee_id = ? AND transaction_timestamp < ?)";
		Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, accountId, payeeId, Timestamp.valueOf(timestamp));
		return exists != null && exists;
	}

	@Override
	public BigDecimal sumAmountUsdByAccountIdAndTimestampBetween(Long accountId, LocalDateTime start, LocalDateTime end) {
		String sql = "SELECT COALESCE(SUM(amount_usd), 0) FROM transactions WHERE account_id = ? AND transaction_timestamp BETWEEN ? AND ?";
		BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class, accountId, Timestamp.valueOf(start), Timestamp.valueOf(end));
		return total == null ? BigDecimal.ZERO : total;
	}
}
