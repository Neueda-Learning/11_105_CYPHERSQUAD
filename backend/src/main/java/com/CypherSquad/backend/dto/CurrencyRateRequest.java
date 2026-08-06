package com.CypherSquad.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CurrencyRateRequest {
	@NotBlank(message = "currencyCode is required")
	@Size(max = 10, message = "currencyCode must not exceed 10 characters")
	private String currencyCode;

	@NotBlank(message = "currencyName is required")
	@Size(max = 80, message = "currencyName must not exceed 80 characters")
	private String currencyName;

	@NotNull(message = "usdRate is required")
	@DecimalMin(value = "0.00000001", message = "usdRate must be greater than zero")
	@Digits(integer = 11, fraction = 8, message = "usdRate must have up to 11 integer digits and 8 decimal places")
	private BigDecimal usdRate;

	private Boolean active;

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

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
}
