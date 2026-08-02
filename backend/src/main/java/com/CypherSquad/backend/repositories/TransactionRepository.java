package com.CypherSquad.backend.repositories;

import com.CypherSquad.backend.models.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
	Transaction save(Transaction transaction);

	List<Transaction> findAll();

	Optional<Transaction> findById(Long transactionId);
}