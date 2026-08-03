package com.CypherSquad.backend.repositories;

import com.CypherSquad.backend.models.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {
	Optional<CurrencyRate> findByCurrencyCodeIgnoreCase(String currencyCode);
}