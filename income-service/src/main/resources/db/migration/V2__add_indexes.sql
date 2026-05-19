CREATE INDEX IF NOT EXISTS idx_incomes_farm_id ON incomes(farm_id);
CREATE INDEX IF NOT EXISTS idx_incomes_user_id ON incomes(user_id);
CREATE INDEX IF NOT EXISTS idx_incomes_occurred_at ON incomes(occurred_at);
CREATE INDEX IF NOT EXISTS idx_incomes_farm_occurred ON incomes(farm_id, occurred_at);