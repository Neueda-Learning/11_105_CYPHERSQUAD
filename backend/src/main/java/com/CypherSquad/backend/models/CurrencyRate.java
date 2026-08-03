package com.CypherSquad.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "currency_rates")
public class CurrencyRate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "currency_id")
	private Long currencyId;

	@Column(name = "currency_code", nullable = false, unique = true, length = 10)
	private String currencyCode;

	@Column(name = "currency_name", nullable = false, length = 80)
	private String currencyName;

	@Column(name = "usd_rate", nullable = false, precision = 19, scale = 8)
	private BigDecimal usdRate;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	public CurrencyRate() {
	}

	public Long getCurrencyId() {
		return currencyId;
	}

	public void setCurrencyId(Long currencyId) {
		this.currencyId = currencyId;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getCurrencyName() {
		return currencyName;
	}

	public void setCurrencyName(String currencyName) {
		this.currencyName = currencyName;
	}

	public BigDecimal getUsdRate() {
		return usdRate;
	}

	public void setUsdRate(BigDecimal usdRate) {
		this.usdRate = usdRate;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}