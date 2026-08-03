package com.CypherSquad.backend.services;

import com.CypherSquad.backend.models.CurrencyRate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CurrencyRateService {
	List<CurrencyRate> getCurrencies();

	CurrencyRate getCurrencyById(Long currencyId);

	CurrencyRate createCurrency(CurrencyRate currencyRate);

	CurrencyRate patchCurrency(Long currencyId, Map<String, Object> patchFields);

	void deleteCurrency(Long currencyId);

	BigDecimal convertToUsd(BigDecimal amount, String currencyCode);
}