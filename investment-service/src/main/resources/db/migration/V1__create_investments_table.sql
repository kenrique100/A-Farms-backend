CREATE TABLE IF NOT EXISTS investments (
                                           id                 BIGSERIAL PRIMARY KEY,
                                           initial_amount     NUMERIC(19, 2) NOT NULL,
    current_balance    NUMERIC(19, 2) NOT NULL,
    farm_id            UUID,
    user_id            UUID,
    created_at         DATE           NOT NULL,
    updated_at         DATE,
    created_timestamp  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_timestamp  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );