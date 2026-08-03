package com.CypherSquad.backend.controllers;

import com.CypherSquad.backend.models.CurrencyRate;
import com.CypherSquad.backend.services.CurrencyRateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/currency")
public class CurrencyRateController {
	private final CurrencyRateService currencyRateService;

	public CurrencyRateController(CurrencyRateService currencyRateService) {
		this.currencyRateService = currencyRateService;
	}

	@GetMapping
	public ResponseEntity<List<CurrencyRate>> getCurrencies() {
		return ResponseEntity.ok(currencyRateService.getCurrencies());
	}

	@PostMapping
	public ResponseEntity<CurrencyRate> createCurrency(@RequestBody CurrencyRate currencyRate) {
		return ResponseEntity.status(HttpStatus.CREATED).body(currencyRateService.createCurrency(currencyRate));
	}

	@GetMapping("/{id}")
	public ResponseEntity<CurrencyRate> getCurrencyById(@PathVariable("id") Long currencyId) {
		return ResponseEntity.ok(currencyRateService.getCurrencyById(currencyId));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<CurrencyRate> patchCurrency(@PathVariable("id") Long currencyId,
		@RequestBody Map<String, Object> patchFields) {
		return ResponseEntity.ok(currencyRateService.patchCurrency(currencyId, patchFields));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCurrency(@PathVariable("id") Long currencyId) {
		currencyRateService.deleteCurrency(currencyId);
		return ResponseEntity.noContent().build();
	}
}