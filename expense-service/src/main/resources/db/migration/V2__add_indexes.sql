CREATE INDEX IF NOT EXISTS idx_expenses_farm_id ON expenses(farm_id);
CREATE INDEX IF NOT EXISTS idx_expenses_user_id ON expenses(user_id);
CREATE INDEX IF NOT EXISTS idx_expenses_occurred_at ON expenses(occurred_at);
CREATE INDEX IF NOT EXISTS idx_expenses_farm_occurred ON expenses(farm_id, occurred_at);