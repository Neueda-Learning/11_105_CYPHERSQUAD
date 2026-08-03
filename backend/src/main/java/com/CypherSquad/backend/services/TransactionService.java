package com.CypherSquad.backend.services;

import com.CypherSquad.backend.models.Transaction;

import java.util.List;

public interface TransactionService {
	Transaction createTransaction(Transaction transaction);

	List<Transaction> getTransactions();

	Transaction getTransactionById(Long transactionId);
}