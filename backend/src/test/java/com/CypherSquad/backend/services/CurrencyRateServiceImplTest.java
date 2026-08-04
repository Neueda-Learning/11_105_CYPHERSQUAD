package com.CypherSquad.backend.services;

import com.CypherSquad.backend.models.CurrencyRate;
import com.CypherSquad.backend.repositories.CurrencyRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyRateServiceImplTest {

	@Mock
	private CurrencyRateRepository currencyRateRepository;

	@InjectMocks
	private CurrencyRateServiceImpl currencyRateService;

	@Test
	void getCurrenciesReturnsRepositoryData() {
		when(currencyRateRepository.findAll()).thenReturn(List.of(sampleCurrency(1L), sampleCurrency(2L)));

		assertEquals(2, currencyRateService.getCurrencies().size());
	}

	@Test
	void getCurrencyByIdThrowsWhenMissing() {
		when(currencyRateRepository.findById(50L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> currencyRateService.getCurrencyById(50L));
	}

	@Test
	void createCurrencyNormalizesCodeAndValidatesRate() {
		CurrencyRate input = sampleCurrency(null);
		input.setCurrencyCode(" kes ");
		when(currencyRateRepository.save(any(CurrencyRate.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CurrencyRate created = currencyRateService.createCurrency(input);

		assertEquals("KES", created.getCurrencyCode());
		assertEquals(new BigDecimal("1.0000"), created.getUsdRate());
	}

	@Test
	void createCurrencyThrowsWhenRateInvalid() {
		CurrencyRate input = sampleCurrency(null);
		input.setUsdRate(BigDecimal.ZERO);

		assertThrows(IllegalArgumentException.class, () -> currencyRateService.createCurrency(input));
		verify(currencyRateRepository, never()).save(any(CurrencyRate.class));
	}

	@Test
	void patchCurrencyUpdatesSupportedFields() {
		CurrencyRate existing = sampleCurrency(3L);
		when(currencyRateRepository.findById(3L)).thenReturn(Optional.of(existing));
		when(currencyRateRepository.save(any(CurrencyRate.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CurrencyRate patched = currencyRateService.patchCurrency(3L, Map.of(
			"currencyCode", " eur ",
			"currencyName", "Euro",
			"usdRate", "1.2300",
			"active", "false"
		));

		assertEquals("EUR", patched.getCurrencyCode());
		assertEquals("Euro", patched.getCurrencyName());
		assertEquals(new BigDecimal("1.2300"), patched.getUsdRate());
		assertEquals(false, patched.isActive());
	}

	@Test
	void deleteCurrencyThrowsWhenMissing() {
		when(currencyRateRepository.findById(4L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> currencyRateService.deleteCurrency(4L));
		verify(currencyRateRepository, never()).deleteById(4L);
	}

	@Test
	void deleteCurrencyCallsRepositoryWhenFound() {
		when(currencyRateRepository.findById(5L)).thenReturn(Optional.of(sampleCurrency(5L)));

		currencyRateService.deleteCurrency(5L);

		verify(currencyRateRepository).deleteById(5L);
	}

	@Test
	void convertToUsdThrowsWhenAmountIsNull() {
		assertThrows(IllegalArgumentException.class,
			() -> currencyRateService.convertToUsd(null, "USD"));
	}

	@Test
	void convertToUsdThrowsWhenCurrencyNotFound() {
		when(currencyRateRepository.findByCurrencyCodeIgnoreCase("USD")).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
			() -> currencyRateService.convertToUsd(new BigDecimal("10.00"), "USD"));
	}

	@Test
	void convertToUsdThrowsWhenCurrencyInactive() {
		CurrencyRate inactive = sampleCurrency(9L);
		inactive.setActive(false);
		when(currencyRateRepository.findByCurrencyCodeIgnoreCase("KES")).thenReturn(Optional.of(inactive));

		assertThrows(IllegalArgumentException.class,
			() -> currencyRateService.convertToUsd(new BigDecimal("10.00"), "KES"));
	}

	@Test
	void convertToUsdReturnsScaledUsdAmount() {
		CurrencyRate rate = sampleCurrency(10L);
		rate.setCurrencyCode("KES");
		rate.setUsdRate(new BigDecimal("0.0078"));
		when(currencyRateRepository.findByCurrencyCodeIgnoreCase("KES")).thenReturn(Optional.of(rate));

		BigDecimal usd = currencyRateService.convertToUsd(new BigDecimal("1000"), "kes");

		assertEquals(new BigDecimal("7.8000"), usd);
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
