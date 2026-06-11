--liquibase formatted sql

--changeset nutro-assist:004-weight-logs
CREATE TABLE weight_logs (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    weight_kg  NUMERIC(5,1) NOT NULL,
    log_date   DATE         NOT NULL,
    notes      TEXT,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, log_date)
);

CREATE INDEX idx_weight_logs_user_date ON weight_logs(user_id, log_date DESC);
