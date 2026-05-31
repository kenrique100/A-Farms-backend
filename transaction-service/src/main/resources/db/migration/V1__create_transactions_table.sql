CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    reference_id BIGINT NOT NULL,
    transaction_date DATE NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    created_by VARCHAR(255),
    farm_id UUID,
    user_id UUID,
    idempotency_key VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
