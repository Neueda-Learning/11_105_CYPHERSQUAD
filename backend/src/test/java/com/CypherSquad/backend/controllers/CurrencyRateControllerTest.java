package com.CypherSquad.backend.controllers;

import com.CypherSquad.backend.models.CurrencyRate;
import com.CypherSquad.backend.services.CurrencyRateService;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CurrencyRateControllerTest {

	@Mock
	private CurrencyRateService currencyRateService;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new CurrencyRateController(currencyRateService)).build();
		objectMapper = new ObjectMapper().findAndRegisterModules();
	}

	@Test
	void getCurrenciesReturnsList() throws Exception {
		when(currencyRateService.getCurrencies()).thenReturn(List.of(sampleCurrency(1L), sampleCurrency(2L)));

		mockMvc.perform(get("/currency"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].currencyId").value(1L));

		verify(currencyRateService).getCurrencies();
	}

	@Test
	void createCurrencyReturnsCreated() throws Exception {
		when(currencyRateService.createCurrency(any(CurrencyRate.class))).thenReturn(sampleCurrency(5L));

		mockMvc.perform(post("/currency")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(sampleCurrency(null))))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.currencyId").value(5L));

		verify(currencyRateService).createCurrency(any(CurrencyRate.class));
	}

	@Test
	void getCurrencyByIdReturnsCurrency() throws Exception {
		when(currencyRateService.getCurrencyById(3L)).thenReturn(sampleCurrency(3L));

		mockMvc.perform(get("/currency/3"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.currencyId").value(3L));

		verify(currencyRateService).getCurrencyById(3L);
	}

	@Test
	void patchCurrencyReturnsUpdatedCurrency() throws Exception {
		when(currencyRateService.patchCurrency(eq(8L), any(Map.class))).thenReturn(sampleCurrency(8L));

		mockMvc.perform(patch("/currency/8")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("currencyName", "Kenyan Shilling"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.currencyId").value(8L));

		verify(currencyRateService).patchCurrency(eq(8L), any(Map.class));
	}

	@Test
	void deleteCurrencyReturnsNoContent() throws Exception {
		mockMvc.perform(delete("/currency/9"))
			.andExpect(status().isNoContent());

		verify(currencyRateService).deleteCurrency(9L);
	}

	private CurrencyRate sampleCurrency(Long id) {
		CurrencyRate currencyRate = new CurrencyRate();
		currencyRate.setCurrencyId(id);
		currencyRate.setCurrencyCode("USD");
		currencyRate.setCurrencyName("US Dollar");
		currencyRate.setUsdRate(new BigDecimal("1.0000"));
		currencyRate.setActive(true);
		return currencyRate;
	}
}
