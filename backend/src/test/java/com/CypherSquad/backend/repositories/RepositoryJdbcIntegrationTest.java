package com.CypherSquad.backend.repositories;

import com.CypherSquad.backend.models.Alert;
import com.CypherSquad.backend.models.CurrencyRate;
import com.CypherSquad.backend.models.Rule;
import com.CypherSquad.backend.models.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class RepositoryJdbcIntegrationTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CurrencyRateRepository currencyRateRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Test
    void transactionRepositorySupportsSaveFindCountExistsAndSum() {
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 10, 0);

        Transaction first = sampleTransaction(1001L, 2001L, new BigDecimal("120.00"), new BigDecimal("120.0000"), base.minusHours(2));
        Transaction second = sampleTransaction(1001L, 2002L, new BigDecimal("80.00"), new BigDecimal("80.0000"), base.minusMinutes(30));
        Transaction third = sampleTransaction(1002L, 2001L, new BigDecimal("40.00"), new BigDecimal("40.0000"), base.minusMinutes(15));

        Transaction savedFirst = transactionRepository.save(first);
        Transaction savedSecond = transactionRepository.save(second);
        transactionRepository.save(third);

        assertNotNull(savedFirst.getTransactionId());
        assertNotNull(savedSecond.getTransactionId());

        savedFirst.setStatus("REVIEWED");
        savedFirst.setCurrency("EUR");
        Transaction updated = transactionRepository.save(savedFirst);
        assertEquals("REVIEWED", updated.getStatus());

        List<Transaction> all = transactionRepository.findAll();
        assertTrue(all.size() >= 3);

        Transaction byId = transactionRepository.findById(savedSecond.getTransactionId()).orElseThrow();
        assertEquals(1001L, byId.getAccountId());

        long count = transactionRepository.countByAccountIdAndTimestampBetween(
            1001L,
            base.minusDays(1),
            base.plusDays(1)
        );
        assertEquals(2L, count);

        boolean existedBefore = transactionRepository.existsByAccountIdAndPayeeIdAndTimestampBefore(
            1001L,
            2002L,
            base
        );
        assertTrue(existedBefore);

        BigDecimal total = transactionRepository.sumAmountUsdByAccountIdAndTimestampBetween(
            1001L,
            base.minusDays(1),
            base.plusDays(1)
        );
        assertEquals(new BigDecimal("200.0000"), total);
    }

    @Test
    void currencyRateRepositorySupportsInsertUpdateFindAndDelete() {
        CurrencyRate kes = new CurrencyRate();
        kes.setCurrencyCode("KES");
        kes.setCurrencyName("Kenyan Shilling");
        kes.setUsdRate(new BigDecimal("0.00780000"));
        kes.setActive(true);

        CurrencyRate saved = currencyRateRepository.save(kes);
        assertNotNull(saved.getCurrencyId());

        CurrencyRate byCode = currencyRateRepository.findByCurrencyCodeIgnoreCase("kes").orElseThrow();
        assertEquals("KES", byCode.getCurrencyCode());

        saved.setCurrencyName("Kenya Shilling");
        saved.setUsdRate(new BigDecimal("0.00790000"));
        saved.setActive(false);
        currencyRateRepository.save(saved);

        CurrencyRate byId = currencyRateRepository.findById(saved.getCurrencyId()).orElseThrow();
        assertEquals("Kenya Shilling", byId.getCurrencyName());
        assertFalse(byId.isActive());

        assertTrue(currencyRateRepository.findAll().size() >= 1);

        currencyRateRepository.deleteById(saved.getCurrencyId());
        assertTrue(currencyRateRepository.findById(saved.getCurrencyId()).isEmpty());
    }

    @Test
    void ruleRepositorySupportsInsertUpdateFindAndActiveFilter() {
        Rule rule = new Rule();
        rule.setRuleName("Daily Amount Limit");
        rule.setRuleType("DAILY_LIMIT");
        rule.setMetric("AMOUNT");
        rule.setComparisonOperator("GREATER_THAN");
        rule.setThreshold(new BigDecimal("500.0000"));
        rule.setSeverity("HIGH");
        rule.setActive(true);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("inclusive", true);
        rule.setParameters(params);

        Rule saved = ruleRepository.save(rule);
        assertNotNull(saved.getRuleId());

        Rule byId = ruleRepository.findById(saved.getRuleId()).orElseThrow();
        assertEquals("DAILY_LIMIT", byId.getRuleType());
        assertTrue(Boolean.TRUE.equals(byId.getParameters().get("inclusive")));

        saved.setActive(false);
        saved.setSeverity("MEDIUM");
        ruleRepository.save(saved);

        Rule updated = ruleRepository.findById(saved.getRuleId()).orElseThrow();
        assertEquals("MEDIUM", updated.getSeverity());
        assertFalse(updated.isActive());

        List<Rule> activeRules = ruleRepository.findByActiveTrue();
        assertTrue(activeRules.stream().noneMatch(r -> r.getRuleId().equals(saved.getRuleId())));

        assertTrue(ruleRepository.findAll().size() >= 1);

        ruleRepository.deleteById(saved.getRuleId());
        assertTrue(ruleRepository.findById(saved.getRuleId()).isEmpty());
    }

    @Test
    void alertRepositorySupportsCreateFindUpdateStatusAndSoftDelete() {
        Transaction tx = transactionRepository.save(sampleTransaction(
            4001L,
            5001L,
            new BigDecimal("30.00"),
            new BigDecimal("30.0000"),
            LocalDateTime.of(2026, 1, 2, 9, 0)
        ));

        Rule rule = new Rule();
        rule.setRuleName("Tx Amount");
        rule.setRuleType("AMOUNT_THRESHOLD");
        rule.setComparisonOperator("GREATER_THAN");
        rule.setThreshold(new BigDecimal("10.0000"));
        rule.setSeverity("HIGH");
        rule.setActive(true);
        rule = ruleRepository.save(rule);

        Alert alert = new Alert();
        alert.setTransactionId(tx.getTransactionId());
        alert.setRuleId(rule.getRuleId());
        alert.setSeverity("HIGH");
        alert.setStatus("OPEN");
        alert.setCreateDate(LocalDateTime.of(2026, 1, 2, 10, 0));
        alert.setNote("created by test");

        Alert created = alertRepository.createAlert(alert);
        assertNotNull(created.getAlertId());

        Alert fetched = alertRepository.getAlertById(created.getAlertId()).orElseThrow();
        assertEquals("OPEN", fetched.getStatus());

        List<Alert> openAlerts = alertRepository.getAlertsByStatus("open");
        assertTrue(openAlerts.stream().anyMatch(a -> a.getAlertId().equals(created.getAlertId())));

        created.setStatus("IN_PROGRESS");
        created.setNote("updated note");
        Alert updated = alertRepository.updateAlert(created.getAlertId(), created);
        assertEquals("IN_PROGRESS", updated.getStatus());

        Alert closed = alertRepository.updateAlertStatus(
            created.getAlertId(),
            "CLOSED",
            LocalDateTime.of(2026, 1, 2, 11, 0)
        );
        assertEquals("CLOSED", closed.getStatus());
        assertNotNull(closed.getCloseDate());

        assertTrue(alertRepository.getAlertByAlertId(created.getAlertId()).isPresent());
        assertTrue(alertRepository.getAlerts().stream().anyMatch(a -> a.getAlertId().equals(created.getAlertId())));

        alertRepository.deleteAlert(created.getAlertId());

        Alert deleted = alertRepository.getAlertById(created.getAlertId()).orElseThrow();
        assertEquals("DELETED", deleted.getStatus());

        assertTrue(alertRepository.getAlerts().stream().noneMatch(a -> a.getAlertId().equals(created.getAlertId())));
    }

    private Transaction sampleTransaction(
        Long accountId,
        Long payeeId,
        BigDecimal amount,
        BigDecimal amountUsd,
        LocalDateTime timestamp
    ) {
        Transaction transaction = new Transaction();
        transaction.setAccountId(accountId);
        transaction.setPayeeId(payeeId);
        transaction.setAmount(amount);
        transaction.setAmountUsd(amountUsd);
        transaction.setTimestamp(timestamp);
        transaction.setCurrency("USD");
        transaction.setType("TRANSFER");
        transaction.setStatus("COMPLETED");
        return transaction;
    }
}
