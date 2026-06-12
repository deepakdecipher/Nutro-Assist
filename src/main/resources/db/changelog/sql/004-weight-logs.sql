--liquibase formatted sql

--changeset nutro-assist:004-weight-logs splitStatements:false
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_name='weight_logs'
CREATE TABLE IF NOT EXISTS weight_logs (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    weight_kg  NUMERIC(5,1) NOT NULL,
    log_date   DATE         NOT NULL,
    notes      TEXT,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, log_date)
);

CREATE INDEX IF NOT EXISTS idx_weight_logs_user_date ON weight_logs(user_id, log_date DESC);
