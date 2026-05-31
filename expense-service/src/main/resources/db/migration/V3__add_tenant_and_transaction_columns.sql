ALTER TABLE expenses ADD COLUMN IF NOT EXISTS farm_id UUID;
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS user_id UUID;
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS transaction_id BIGINT;
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_expenses_farm_id ON expenses(farm_id);
CREATE INDEX IF NOT EXISTS idx_expenses_user_id ON expenses(user_id);
CREATE INDEX IF NOT EXISTS idx_expenses_farm_occurred ON expenses(farm_id, occurred_at);
