CREATE TABLE IF NOT EXISTS transactions (
    transaction_id BIGINT NOT NULL AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    payee_id BIGINT,
    amount DECIMAL(19, 4) NOT NULL,
    amount_usd DECIMAL(19, 4),
    transaction_timestamp DATETIME NOT NULL,
    currency VARCHAR(10),
    type VARCHAR(50),
    status VARCHAR(50),
    PRIMARY KEY (transaction_id)
);

SET @add_amount_usd_column = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'transactions'
              AND column_name = 'amount_usd'
        ),
        'SELECT 1',
        'ALTER TABLE transactions ADD COLUMN amount_usd DECIMAL(19, 4) NULL'
    )
);
PREPARE stmt_add_amount_usd_column FROM @add_amount_usd_column;
EXECUTE stmt_add_amount_usd_column;
DEALLOCATE PREPARE stmt_add_amount_usd_column;

CREATE TABLE IF NOT EXISTS currency_rates (
    currency_id BIGINT NOT NULL AUTO_INCREMENT,
    currency_code VARCHAR(10) NOT NULL,
    currency_name VARCHAR(80) NOT NULL,
    usd_rate DECIMAL(19, 8) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (currency_id),
    UNIQUE KEY uk_currency_rates_code (currency_code)
);

INSERT INTO currency_rates (currency_code, currency_name, usd_rate, active)
VALUES ('USD', 'US Dollar', 1.00000000, TRUE)
ON DUPLICATE KEY UPDATE
    currency_name = VALUES(currency_name),
    usd_rate = VALUES(usd_rate),
    active = VALUES(active);

CREATE TABLE IF NOT EXISTS rules (
    rule_id BIGINT NOT NULL AUTO_INCREMENT,
    rule_name VARCHAR(120) NOT NULL,
    rule_type VARCHAR(80) NOT NULL,
    metric VARCHAR(80),
    comparison_operator VARCHAR(30),
    threshold DECIMAL(19, 4),
    minimum_threshold DECIMAL(19, 4),
    maximum_threshold DECIMAL(19, 4),
    transaction_count_threshold INT,
    time_window_value INT,
    time_window_unit VARCHAR(20),
    payee_scope VARCHAR(40),
    severity VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    parameters_json LONGTEXT,
    PRIMARY KEY (rule_id)
);

CREATE TABLE IF NOT EXISTS alerts (
    alert_id BIGINT NOT NULL AUTO_INCREMENT,
    transaction_id BIGINT,
    rule_id BIGINT,
    severity VARCHAR(20),
    status VARCHAR(20),
    create_date DATETIME,
    close_date DATETIME,
    note VARCHAR(1000),
    PRIMARY KEY (alert_id),
    CONSTRAINT fk_alert_transaction FOREIGN KEY (transaction_id) REFERENCES transactions (transaction_id),
    CONSTRAINT fk_alert_rule FOREIGN KEY (rule_id) REFERENCES rules (rule_id)
);

SET @create_alert_status_idx = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'alerts'
              AND index_name = 'idx_alert_status'
        ),
        'SELECT 1',
        'CREATE INDEX idx_alert_status ON alerts (status)'
    )
);
PREPARE stmt_alert_status_idx FROM @create_alert_status_idx;
EXECUTE stmt_alert_status_idx;
DEALLOCATE PREPARE stmt_alert_status_idx;

SET @create_txn_account_idx = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'transactions'
              AND index_name = 'idx_transaction_account_id'
        ),
        'SELECT 1',
        'CREATE INDEX idx_transaction_account_id ON transactions (account_id)'
    )
);
PREPARE stmt_txn_account_idx FROM @create_txn_account_idx;
EXECUTE stmt_txn_account_idx;
DEALLOCATE PREPARE stmt_txn_account_idx;