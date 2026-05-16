ALTER TABLE transactions ADD COLUMN IF NOT EXISTS farm_id UUID;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS user_id UUID;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS type VARCHAR(40) DEFAULT 'MANUAL';
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS source_service VARCHAR(60) DEFAULT 'TRANSACTION_SERVICE';
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS source_reference_id BIGINT;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_transactions_farm_id ON transactions(farm_id);
CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_source_service_ref ON transactions(source_service, source_reference_id);
