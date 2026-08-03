-- Use this script in MySQL Workbench for manual SQL operations.

-- Read all currencies and conversion rates
SELECT * FROM currency_rates ORDER BY currency_code;

-- Insert a currency rate (1 unit of this currency equals usd_rate USD)
INSERT INTO currency_rates (currency_code, currency_name, usd_rate, active)
VALUES ('INR', 'Indian Rupee', 0.01200000, TRUE)
ON DUPLICATE KEY UPDATE
    currency_name = VALUES(currency_name),
    usd_rate = VALUES(usd_rate),
    active = VALUES(active);

-- Update a currency rate
UPDATE currency_rates
SET usd_rate = 0.01195000, active = TRUE
WHERE currency_code = 'INR';

-- Delete a currency
DELETE FROM currency_rates WHERE currency_code = 'INR';

-- Read all transactions
SELECT * FROM transactions ORDER BY transaction_id;

-- Insert a transaction
INSERT INTO transactions (account_id, payee_id, amount, amount_usd, transaction_timestamp, currency, type, status)
VALUES (1001, 2001, 1250.5000, 1250.5000, NOW(), 'USD', 'DEBIT', 'SUCCESS');

-- Read transaction by id
SELECT * FROM transactions WHERE transaction_id = 1;

-- Read all rules
SELECT * FROM rules ORDER BY rule_id;

-- Insert a rule
INSERT INTO rules (
    rule_name,
    rule_type,
    metric,
    comparison_operator,
    threshold,
    transaction_count_threshold,
    time_window_value,
    time_window_unit,
    payee_scope,
    severity,
    active,
    parameters_json
)
VALUES (
    'High Amount Rule',
    'AMOUNT_THRESHOLD',
    'amount',
    'GREATER_THAN',
    10000.0000,
    NULL,
    NULL,
    NULL,
    NULL,
    'HIGH',
    TRUE,
    '{"currency":"USD"}'
);

-- Update a rule
UPDATE rules
SET severity = 'CRITICAL', active = TRUE
WHERE rule_id = 1;

-- Delete a rule
DELETE FROM rules WHERE rule_id = 1;

-- Read all alerts
SELECT * FROM alerts ORDER BY alert_id;

-- Read alerts by status
SELECT * FROM alerts WHERE LOWER(status) = LOWER('OPEN') ORDER BY alert_id;

-- Insert an alert
INSERT INTO alerts (transaction_id, rule_id, severity, status, create_date, note)
VALUES (1, 1, 'HIGH', 'OPEN', NOW(), 'Flagged by amount threshold rule');

-- Update alert
UPDATE alerts
SET status = 'CLOSED', close_date = NOW(), note = 'Reviewed and closed'
WHERE alert_id = 1;

-- Delete alert
DELETE FROM alerts WHERE alert_id = 1;
