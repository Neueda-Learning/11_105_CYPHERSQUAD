package com.CypherSquad.backend.controllers;

import com.CypherSquad.backend.models.Transaction;
import com.CypherSquad.backend.services.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

	@Mock
	private TransactionService transactionService;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new TransactionController(transactionService)).build();
		objectMapper = new ObjectMapper().findAndRegisterModules();
	}

	@Test
	void createTransactionReturnsCreated() throws Exception {
		Transaction created = sampleTransaction(1L);
		when(transactionService.createTransaction(any(Transaction.class))).thenReturn(created);

		mockMvc.perform(post("/transactions")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(sampleTransaction(null))))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.transactionId").value(1L))
			.andExpect(jsonPath("$.currency").value("USD"));

		verify(transactionService).createTransaction(any(Transaction.class));
	}

	@Test
	void getTransactionsReturnsList() throws Exception {
		when(transactionService.getTransactions()).thenReturn(List.of(sampleTransaction(1L), sampleTransaction(2L)));

		mockMvc.perform(get("/transactions"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].transactionId").value(1L))
			.andExpect(jsonPath("$[1].transactionId").value(2L));

		verify(transactionService).getTransactions();
	}

	@Test
	void getTransactionByIdReturnsTransaction() throws Exception {
		when(transactionService.getTransactionById(7L)).thenReturn(sampleTransaction(7L));

		mockMvc.perform(get("/transactions/7"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.transactionId").value(7L));

		verify(transactionService).getTransactionById(7L);
	}

	private Transaction sampleTransaction(Long id) {
		Transaction transaction = new Transaction();
		transaction.setTransactionId(id);
		transaction.setAccountId(11L);
		transaction.setAmount(new BigDecimal("100.00"));
		transaction.setAmountUsd(new BigDecimal("100.0000"));
		transaction.setCurrency("USD");
		transaction.setTimestamp(LocalDateTime.of(2026, 1, 1, 10, 0));
		transaction.setType("TRANSFER");
		transaction.setStatus("COMPLETED");
		return transaction;
	}
}
