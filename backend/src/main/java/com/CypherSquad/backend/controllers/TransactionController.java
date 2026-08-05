package com.CypherSquad.backend.controllers;

import com.CypherSquad.backend.dto.TransactionRequest;
import com.CypherSquad.backend.models.Transaction;
import com.CypherSquad.backend.services.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
	private final TransactionService transactionService;

	public TransactionController(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@PostMapping
	public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody TransactionRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createTransaction(toTransaction(request)));
	}

	@GetMapping
	public ResponseEntity<List<Transaction>> getTransactions() {
		return ResponseEntity.ok(transactionService.getTransactions());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Transaction> getTransactionById(@PathVariable("id") Long transactionId) {
		return ResponseEntity.ok(transactionService.getTransactionById(transactionId));
	}

	private Transaction toTransaction(TransactionRequest request) {
		Transaction transaction = new Transaction();
		transaction.setAccountId(request.getAccountId());
		transaction.setPayeeId(request.getPayeeId());
		transaction.setAmount(request.getAmount());
		transaction.setTimestamp(request.getTimestamp());
		transaction.setCurrency(request.getCurrency());
		transaction.setType(request.getType());
		transaction.setStatus(request.getStatus());
		return transaction;
	}
}