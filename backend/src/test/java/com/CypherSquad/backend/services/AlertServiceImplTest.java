package com.CypherSquad.backend.services;

import com.CypherSquad.backend.models.Alert;
import com.CypherSquad.backend.repositories.AlertRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceImplTest {

	@Mock
	private AlertRepository alertRepository;

	@InjectMocks
	private AlertServiceImpl alertService;

	@Test
	void createAlertSetsDefaultStatusAndCreateDateWhenMissing() {
		Alert alert = new Alert();
		when(alertRepository.createAlert(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Alert created = alertService.createAlert(alert);

		assertEquals("OPEN", created.getStatus());
		assertNotNull(created.getCreateDate());
		verify(alertRepository).createAlert(alert);
	}

	@Test
	void createAlertPreservesProvidedStatusAndDate() {
		Alert alert = sampleAlert(1L);
		LocalDateTime originalDate = alert.getCreateDate();
		when(alertRepository.createAlert(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Alert created = alertService.createAlert(alert);

		assertEquals("CLOSED", created.getStatus());
		assertEquals(originalDate, created.getCreateDate());
	}

	@Test
	void getAlertsReturnsRepositoryData() {
		when(alertRepository.getAlerts()).thenReturn(List.of(sampleAlert(1L), sampleAlert(2L)));

		assertEquals(2, alertService.getAlerts().size());
		verify(alertRepository).getAlerts();
	}

	@Test
	void getAlertByIdThrowsWhenMissing() {
		when(alertRepository.getAlertById(99L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> alertService.getAlertById(99L));
	}

	@Test
	void getAlertsByStatusReturnsRepositoryData() {
		when(alertRepository.getAlertsByStatus("OPEN")).thenReturn(List.of(sampleAlert(7L)));

		assertEquals(1, alertService.getAlertsByStatus("OPEN").size());
		verify(alertRepository).getAlertsByStatus("OPEN");
	}

	@Test
	void updateAlertCopiesIdAndCreateDateFromExistingWhenMissing() {
		Alert existing = sampleAlert(10L);
		existing.setCreateDate(LocalDateTime.of(2026, 1, 2, 10, 0));
		Alert incoming = new Alert();
		incoming.setStatus("OPEN");
		when(alertRepository.getAlertById(10L)).thenReturn(Optional.of(existing));
		when(alertRepository.updateAlert(eq(10L), any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(1));

		Alert updated = alertService.updateAlert(10L, incoming);

		assertEquals(10L, updated.getAlertId());
		assertEquals(existing.getCreateDate(), updated.getCreateDate());
	}

	@Test
	void updateAlertStatusThrowsWhenStatusBlank() {
		assertThrows(IllegalArgumentException.class, () -> alertService.updateAlertStatus(1L, "   "));
		verify(alertRepository, never()).updateAlertStatus(anyLong(), any(), any());
	}

	@Test
	void updateAlertStatusNormalizesAndSetsCloseDateForClosedStatuses() {
		Alert existing = sampleAlert(12L);
		when(alertRepository.getAlertById(12L)).thenReturn(Optional.of(existing));
		when(alertRepository.updateAlertStatus(eq(12L), eq("CLOSED"), any(LocalDateTime.class))).thenReturn(existing);

		alertService.updateAlertStatus(12L, " closed ");

		ArgumentCaptor<LocalDateTime> closeDateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
		verify(alertRepository).updateAlertStatus(eq(12L), eq("CLOSED"), closeDateCaptor.capture());
		assertNotNull(closeDateCaptor.getValue());
	}

	@Test
	void updateAlertStatusLeavesCloseDateNullForOpenStatus() {
		Alert existing = sampleAlert(13L);
		when(alertRepository.getAlertById(13L)).thenReturn(Optional.of(existing));
		when(alertRepository.updateAlertStatus(eq(13L), eq("OPEN"), eq(null))).thenReturn(existing);

		alertService.updateAlertStatus(13L, "open");

		verify(alertRepository).updateAlertStatus(13L, "OPEN", null);
	}

	@Test
	void deleteAlertThrowsWhenMissing() {
		when(alertRepository.getAlertById(45L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> alertService.deleteAlert(45L));
		verify(alertRepository, never()).deleteAlert(anyLong());
	}

	@Test
	void deleteAlertCallsRepositoryWhenExists() {
		when(alertRepository.getAlertById(46L)).thenReturn(Optional.of(sampleAlert(46L)));

		alertService.deleteAlert(46L);

		verify(alertRepository).deleteAlert(46L);
	}

	@Test
	void getAlertByAlertIdThrowsWhenMissing() {
		when(alertRepository.getAlertByAlertId(77L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> alertService.getAlertByAlertId(77L));
	}

	@Test
	void getAlertByAlertIdReturnsAlertWhenFound() {
		Alert alert = sampleAlert(78L);
		when(alertRepository.getAlertByAlertId(78L)).thenReturn(Optional.of(alert));

		Alert found = alertService.getAlertByAlertId(78L);

		assertEquals(78L, found.getAlertId());
	}

	private Alert sampleAlert(Long id) {
		Alert alert = new Alert();
		alert.setAlertId(id);
		alert.setStatus("CLOSED");
		alert.setCreateDate(LocalDateTime.of(2026, 1, 1, 10, 0));
		alert.setCloseDate(LocalDateTime.of(2026, 1, 2, 10, 0));
		return alert;
	}
}
