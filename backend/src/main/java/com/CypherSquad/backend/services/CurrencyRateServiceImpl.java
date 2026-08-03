package com.CypherSquad.backend.services;

import com.CypherSquad.backend.models.CurrencyRate;
import com.CypherSquad.backend.repositories.CurrencyRateRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CurrencyRateServiceImpl implements CurrencyRateService {
	private final CurrencyRateRepository currencyRateRepository;

	public CurrencyRateServiceImpl(CurrencyRateRepository currencyRateRepository) {
		this.currencyRateRepository = currencyRateRepository;
	}

	@Override
	public List<CurrencyRate> getCurrencies() {
		return currencyRateRepository.findAll();
	}

	@Override
	public CurrencyRate getCurrencyById(Long currencyId) {
		return currencyRateRepository.findById(currencyId)
			.orElseThrow(() -> new ResourceNotFoundException("Currency not found: " + currencyId));
	}

	@Override
	public CurrencyRate createCurrency(CurrencyRate currencyRate) {
		currencyRate.setCurrencyCode(normalizeCode(currencyRate.getCurrencyCode()));
		validateUsdRate(currencyRate.getUsdRate());
		return currencyRateRepository.save(currencyRate);
	}

	@Override
	public CurrencyRate patchCurrency(Long currencyId, Map<String, Object> patchFields) {
		CurrencyRate existing = getCurrencyById(currencyId);

		if (patchFields.containsKey("currencyCode")) {
			existing.setCurrencyCode(normalizeCode((String) patchFields.get("currencyCode")));
		}
		if (patchFields.containsKey("currencyName")) {
			existing.setCurrencyName((String) patchFields.get("currencyName"));
		}
		if (patchFields.containsKey("usdRate")) {
			BigDecimal usdRate = new BigDecimal(String.valueOf(patchFields.get("usdRate")));
			validateUsdRate(usdRate);
			existing.setUsdRate(usdRate);
		}
		if (patchFields.containsKey("active")) {
			existing.setActive(Boolean.parseBoolean(String.valueOf(patchFields.get("active"))));
		}

		return currencyRateRepository.save(existing);
	}

	@Override
	public void deleteCurrency(Long currencyId) {
		if (currencyRateRepository.findById(currencyId).isEmpty()) {
			throw new ResourceNotFoundException("Currency not found: " + currencyId);
		}
		currencyRateRepository.deleteById(currencyId);
	}

	@Override
	public BigDecimal convertToUsd(BigDecimal amount, String currencyCode) {
		if (amount == null) {
			throw new IllegalArgumentException("Transaction amount cannot be null");
		}
		String normalizedCode = normalizeCode(currencyCode);
		CurrencyRate currencyRate = currencyRateRepository.findByCurrencyCodeIgnoreCase(normalizedCode)
			.orElseThrow(() -> new ResourceNotFoundException("Currency not found: " + normalizedCode));

		if (!currencyRate.isActive()) {
			throw new IllegalArgumentException("Currency is inactive: " + normalizedCode);
		}

		return amount.multiply(currencyRate.getUsdRate()).setScale(4, RoundingMode.HALF_UP);
	}

	private String normalizeCode(String currencyCode) {
		if (currencyCode == null || currencyCode.isBlank()) {
			return "USD";
		}
		return currencyCode.trim().toUpperCase(Locale.ROOT);
	}

	private void validateUsdRate(BigDecimal usdRate) {
		if (usdRate == null || usdRate.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("usdRate must be greater than zero");
		}
	}
}