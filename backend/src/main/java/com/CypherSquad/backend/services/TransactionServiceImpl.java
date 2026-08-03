package com.CypherSquad.backend.services;

import com.CypherSquad.backend.models.Transaction;
import com.CypherSquad.backend.repositories.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {
	private final TransactionRepository transactionRepository;
	private final CurrencyRateService currencyRateService;
	private final RuleService ruleService;

	public TransactionServiceImpl(TransactionRepository transactionRepository,
		CurrencyRateService currencyRateService,
		RuleService ruleService) {
		this.transactionRepository = transactionRepository;
		this.currencyRateService = currencyRateService;
		this.ruleService = ruleService;
	}

	@Override
	public Transaction createTransaction(Transaction transaction) {
		if (transaction.getTimestamp() == null) {
			transaction.setTimestamp(LocalDateTime.now());
		}

		String normalizedCurrency = transaction.getCurrency() == null || transaction.getCurrency().isBlank()
			? "USD"
			: transaction.getCurrency().trim().toUpperCase(Locale.ROOT);
		transaction.setCurrency(normalizedCurrency);
		transaction.setAmountUsd(currencyRateService.convertToUsd(transaction.getAmount(), normalizedCurrency));
		Transaction createdTransaction = transactionRepository.save(transaction);
		ruleService.evaluateActiveRulesForTransaction(createdTransaction.getTransactionId());
		return createdTransaction;
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