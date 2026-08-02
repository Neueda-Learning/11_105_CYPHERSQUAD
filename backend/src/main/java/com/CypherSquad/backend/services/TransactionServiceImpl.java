package com.CypherSquad.backend.services;

import com.CypherSquad.backend.models.Transaction;
import com.CypherSquad.backend.repositories.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {
	private final TransactionRepository transactionRepository;

	public TransactionServiceImpl(TransactionRepository transactionRepository) {
		this.transactionRepository = transactionRepository;
	}

	@Override
	public Transaction createTransaction(Transaction transaction) {
		if (transaction.getTimestamp() == null) {
			transaction.setTimestamp(LocalDateTime.now());
		}
		return transactionRepository.save(transaction);
	}

	@Override
	public List<Transaction> getTransactions() {
		return transactionRepository.findAll();
	}

	@Override
	public Transaction getTransactionById(Long transactionId) {
		return transactionRepository.findById(transactionId)
			.orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));
	}
}