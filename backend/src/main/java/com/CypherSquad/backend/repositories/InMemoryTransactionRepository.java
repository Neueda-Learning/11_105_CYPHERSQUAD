package com.CypherSquad.backend.repositories;

import com.CypherSquad.backend.models.Transaction;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {
	private final ConcurrentMap<Long, Transaction> storage = new ConcurrentHashMap<>();
	private final AtomicLong sequence = new AtomicLong(0);

	@Override
	public Transaction save(Transaction transaction) {
		Long transactionId = transaction.getTransactionId();
		if (transactionId == null) {
			transactionId = sequence.incrementAndGet();
			transaction.setTransactionId(transactionId);
		} else {
			Long resolvedTransactionId = transactionId;
			sequence.updateAndGet(current -> Math.max(current, resolvedTransactionId));
		}
		storage.put(transactionId, transaction);
		return transaction;
	}

	@Override
	public List<Transaction> findAll() {
		List<Transaction> transactions = new ArrayList<>(storage.values());
		transactions.sort(Comparator.comparing(Transaction::getTransactionId));
		return transactions;
	}

	@Override
	public Optional<Transaction> findById(Long transactionId) {
		return Optional.ofNullable(storage.get(transactionId));
	}
}