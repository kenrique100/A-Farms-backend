CREATE INDEX IF NOT EXISTS idx_investments_farm_id    ON investments (farm_id);
CREATE INDEX IF NOT EXISTS idx_investments_user_id    ON investments (user_id);
CREATE INDEX IF NOT EXISTS idx_investments_created_at ON investments (created_at);
CREATE INDEX IF NOT EXISTS idx_investments_farm_date  ON investments (farm_id, created_at);