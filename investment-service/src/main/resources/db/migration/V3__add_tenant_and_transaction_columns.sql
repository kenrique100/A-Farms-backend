ALTER TABLE investments ADD COLUMN IF NOT EXISTS farm_id UUID;
ALTER TABLE investments ADD COLUMN IF NOT EXISTS user_id UUID;
ALTER TABLE investments ADD COLUMN IF NOT EXISTS transaction_id BIGINT;
ALTER TABLE investments ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_investments_farm_id ON investments(farm_id);
CREATE INDEX IF NOT EXISTS idx_investments_user_id ON investments(user_id);
CREATE INDEX IF NOT EXISTS idx_investments_farm_occurred ON investments(farm_id, occurred_at);
