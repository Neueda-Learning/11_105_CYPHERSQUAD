package com.CypherSquad.backend.repositories;

import com.CypherSquad.backend.models.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
	Transaction save(Transaction transaction);

	List<Transaction> findAll();

	Optional<Transaction> findById(Long transactionId);

	long countByAccountIdAndTimestampBetween(Long accountId, LocalDateTime start, LocalDateTime end);

	boolean existsByAccountIdAndPayeeIdAndTimestampBefore(Long accountId, Long payeeId, LocalDateTime timestamp);

	BigDecimal sumAmountUsdByAccountIdAndTimestampBetween(Long accountId, LocalDateTime start, LocalDateTime end);
}