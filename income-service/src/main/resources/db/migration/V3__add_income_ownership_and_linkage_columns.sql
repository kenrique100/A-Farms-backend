ALTER TABLE incomes ADD COLUMN IF NOT EXISTS farm_id UUID;
ALTER TABLE incomes ADD COLUMN IF NOT EXISTS user_id UUID;
ALTER TABLE incomes ADD COLUMN IF NOT EXISTS transaction_id BIGINT;
ALTER TABLE incomes ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_incomes_farm_id ON incomes(farm_id);
CREATE INDEX IF NOT EXISTS idx_incomes_user_id ON incomes(user_id);
CREATE INDEX IF NOT EXISTS idx_incomes_transaction_id ON incomes(transaction_id);
