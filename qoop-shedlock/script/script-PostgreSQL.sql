-- PostgreSQL DDL Script for Shedlock

CREATE TABLE IF NOT EXISTS shedlock (
                                        name       VARCHAR(64) NOT NULL PRIMARY KEY,
    lock_until TIMESTAMP(3) NOT NULL,
    locked_at  TIMESTAMP(3) NOT NULL,
    locked_by  VARCHAR(255) NOT NULL
    );

-- Indexes
CREATE INDEX IF NOT EXISTS idx_shedlock_lock_until ON shedlock(lock_until);
CREATE INDEX IF NOT EXISTS idx_shedlock_locked_at ON shedlock(locked_at);
CREATE INDEX IF NOT EXISTS idx_shedlock_locked_by ON shedlock(locked_by);