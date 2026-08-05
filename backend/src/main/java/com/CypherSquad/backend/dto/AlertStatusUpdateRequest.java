package com.CypherSquad.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AlertStatusUpdateRequest {
	@NotBlank(message = "status is required")
	@Size(max = 20, message = "status must not exceed 20 characters")
	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
