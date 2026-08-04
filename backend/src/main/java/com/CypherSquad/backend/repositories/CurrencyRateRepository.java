package com.CypherSquad.backend.repositories;

import com.CypherSquad.backend.models.CurrencyRate;

import java.util.List;
import java.util.Optional;


public interface CurrencyRateRepository {
	CurrencyRate save(CurrencyRate currencyRate);

	List<CurrencyRate> findAll();

	Optional<CurrencyRate> findById(Long currencyId);

	void deleteById(Long currencyId);

	Optional<CurrencyRate> findByCurrencyCodeIgnoreCase(String currencyCode);
}