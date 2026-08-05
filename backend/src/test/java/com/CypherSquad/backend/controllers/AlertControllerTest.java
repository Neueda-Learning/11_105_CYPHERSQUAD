package com.CypherSquad.backend.controllers;

import com.CypherSquad.backend.models.Alert;
import com.CypherSquad.backend.services.AlertService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AlertControllerTest {

	@Mock
	private AlertService alertService;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new AlertController(alertService)).build();
		objectMapper = new ObjectMapper().findAndRegisterModules();
	}

	@Test
	void getAlertsReturnsList() throws Exception {
		when(alertService.getAlerts()).thenReturn(List.of(sampleAlert(1L), sampleAlert(2L)));

		mockMvc.perform(get("/alerts"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].alertId").value(1L));

		verify(alertService).getAlerts();
	}

	@Test
	void getAlertByIdReturnsAlert() throws Exception {
		when(alertService.getAlertById(3L)).thenReturn(sampleAlert(3L));

		mockMvc.perform(get("/alerts/3"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.alertId").value(3L));

		verify(alertService).getAlertById(3L);
	}

	@Test
	void updateAlertReturnsUpdatedAlert() throws Exception {
		when(alertService.updateAlert(any(Long.class), any(Alert.class))).thenReturn(sampleAlert(4L));

		mockMvc.perform(put("/alerts/4")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(sampleAlert(null))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.alertId").value(4L));

		verify(alertService).updateAlert(any(Long.class), any(Alert.class));
	}

	@Test
	void updateAlertStatusReturnsUpdatedAlert() throws Exception {
		Alert updated = sampleAlert(5L);
		updated.setStatus("CLOSED");
		when(alertService.updateAlertStatus(5L, "CLOSED")).thenReturn(updated);

		mockMvc.perform(put("/alerts/5/status")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("status", "CLOSED"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("CLOSED"));

		verify(alertService).updateAlertStatus(5L, "CLOSED");
	}

	@Test
	void getAlertsByStatusReturnsList() throws Exception {
		AlertController controller = new AlertController(alertService);
		when(alertService.getAlertsByStatus("OPEN")).thenReturn(List.of(sampleAlert(6L)));

		ResponseEntity<List<Alert>> response = controller.getAlertsByStatus("OPEN");

		assertEquals(200, response.getStatusCode().value());
		assertEquals(1, response.getBody().size());
		assertEquals(6L, response.getBody().get(0).getAlertId());

		verify(alertService).getAlertsByStatus("OPEN");
	}

	@Test
	void deleteAlertReturnsNoContent() throws Exception {
		mockMvc.perform(delete("/alerts/7"))
			.andExpect(status().isNoContent());

		verify(alertService).deleteAlert(7L);
	}

	@Test
	void getAlertByAlertIdReturnsAlert() throws Exception {
		when(alertService.getAlertByAlertId(88L)).thenReturn(sampleAlert(88L));

		mockMvc.perform(get("/alerts/alertId/88"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.alertId").value(88L));

		verify(alertService).getAlertByAlertId(88L);
	}

	private Alert sampleAlert(Long id) {
		Alert alert = new Alert();
		alert.setAlertId(id);
		alert.setTransactionId(101L);
		alert.setRuleId(202L);
		alert.setSeverity("HIGH");
		alert.setStatus("OPEN");
		alert.setCreateDate(LocalDateTime.of(2026, 1, 1, 11, 0));
		alert.setNote("Threshold exceeded");
		return alert;
	}
}
