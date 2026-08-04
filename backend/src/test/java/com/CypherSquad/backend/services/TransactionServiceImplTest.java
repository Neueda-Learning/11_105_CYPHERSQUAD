package com.CypherSquad.backend.services;

import com.CypherSquad.backend.models.Transaction;
import com.CypherSquad.backend.repositories.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

	@Mock
	private TransactionRepository transactionRepository;

	@Mock
	private CurrencyRateService currencyRateService;

	@Mock
	private RuleService ruleService;

	@InjectMocks
	private TransactionServiceImpl transactionService;

	@Test
	void createTransactionDefaultsTimestampAndCurrencyAndTriggersRuleEvaluation() {
		Transaction tx = new Transaction();
		tx.setAccountId(11L);
		tx.setAmount(new BigDecimal("100.00"));
		when(currencyRateService.convertToUsd(new BigDecimal("100.00"), "USD")).thenReturn(new BigDecimal("100.0000"));
		when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
			Transaction saved = invocation.getArgument(0);
			saved.setTransactionId(500L);
			return saved;
		});

		Transaction created = transactionService.createTransaction(tx);

		assertEquals(500L, created.getTransactionId());
		assertEquals("USD", created.getCurrency());
		assertNotNull(created.getTimestamp());
		assertEquals(new BigDecimal("100.0000"), created.getAmountUsd());
		verify(ruleService).evaluateActiveRulesForTransaction(500L);
	}

	@Test
	void createTransactionNormalizesCurrencyBeforeConversion() {
		Transaction tx = new Transaction();
		tx.setAccountId(12L);
		tx.setAmount(new BigDecimal("200.00"));
		tx.setCurrency(" eur ");
		tx.setTimestamp(LocalDateTime.of(2026, 1, 1, 12, 0));
		when(currencyRateService.convertToUsd(new BigDecimal("200.00"), "EUR")).thenReturn(new BigDecimal("234.5600"));
		when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
			Transaction saved = invocation.getArgument(0);
			saved.setTransactionId(501L);
			return saved;
		});

		transactionService.createTransaction(tx);

		ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
		verify(transactionRepository).save(captor.capture());
		assertEquals("EUR", captor.getValue().getCurrency());
		assertEquals(LocalDateTime.of(2026, 1, 1, 12, 0), captor.getValue().getTimestamp());
	}

	@Test
	void getTransactionsReturnsRepositoryData() {
		when(transactionRepository.findAll()).thenReturn(List.of(new Transaction(), new Transaction()));

		assertEquals(2, transactionService.getTransactions().size());
	}

	@Test
	void getTransactionByIdThrowsWhenMissing() {
		when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactionById(999L));
	}

	@Test
	void getTransactionByIdReturnsTransactionWhenFound() {
		Transaction tx = new Transaction();
		tx.setTransactionId(77L);
		when(transactionRepository.findById(77L)).thenReturn(Optional.of(tx));

		assertEquals(77L, transactionService.getTransactionById(77L).getTransactionId());
	}
}
