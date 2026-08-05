package com.CypherSquad.backend.controllers;

import com.CypherSquad.backend.models.Transaction;
import com.CypherSquad.backend.services.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {
	private final TransactionService transactionService;

	public TransactionController(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@PostMapping
	public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
		return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createTransaction(transaction));
	}

	@GetMapping
	public ResponseEntity<List<Transaction>> getTransactions() {
		return ResponseEntity.ok(transactionService.getTransactions());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Transaction> getTransactionById(@PathVariable("id") Long transactionId) {
		return ResponseEntity.ok(transactionService.getTransactionById(transactionId));
	}
}