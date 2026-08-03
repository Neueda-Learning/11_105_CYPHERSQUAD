package com.CypherSquad.backend.repositories;

import com.CypherSquad.backend.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
	long countByAccountIdAndTimestampBetween(Long accountId, LocalDateTime start, LocalDateTime end);

	boolean existsByAccountIdAndPayeeIdAndTimestampBefore(Long accountId, Long payeeId, LocalDateTime timestamp);

	@Query("SELECT COALESCE(SUM(t.amountUsd), 0) FROM Transaction t WHERE t.accountId = :accountId AND t.timestamp BETWEEN :start AND :end")
	BigDecimal sumAmountUsdByAccountIdAndTimestampBetween(@Param("accountId") Long accountId,
		@Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end);
}