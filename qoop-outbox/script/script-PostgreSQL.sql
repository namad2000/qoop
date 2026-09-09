-- PostgreSQL DDL Script (Complete)

CREATE TABLE IF NOT EXISTS outbox_event (
    id            UUID PRIMARY KEY,
    aggregatetype VARCHAR(100) NOT NULL,
    aggregateid   VARCHAR(100) NOT NULL,
    type          VARCHAR(500) NOT NULL,
    topic         VARCHAR(200) NOT NULL,
    payload       JSONB NOT NULL,
    headers       JSONB,
    status        VARCHAR(20) NOT NULL DEFAULT 'NEW',
    retry_count   INT NOT NULL DEFAULT 0,
    version       BIGINT NOT NULL DEFAULT 0,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    sent_at       TIMESTAMP
);

CREATE TABLE IF NOT EXISTS outbox_error_message (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    topic           VARCHAR(200),
    key             VARCHAR(200),
    payload         JSONB,
    error_message   VARCHAR(2000),
    exception_class VARCHAR(500),
    stack_trace     TEXT,
    correlation_id  VARCHAR(100),
    timestamp       TIMESTAMPTZ
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_outbox_status_created ON outbox_event(status, created_at);
CREATE INDEX IF NOT EXISTS idx_outbox_aggregate ON outbox_event(aggregatetype, aggregateid);
CREATE INDEX IF NOT EXISTS idx_outbox_topic ON outbox_event(topic);
CREATE INDEX IF NOT EXISTS idx_outbox_error_corr ON outbox_error_message(correlation_id);
CREATE INDEX IF NOT EXISTS idx_outbox_error_time ON outbox_error_message(timestamp);