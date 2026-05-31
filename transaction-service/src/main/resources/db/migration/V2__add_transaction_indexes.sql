CREATE INDEX IF NOT EXISTS idx_transactions_farm_id ON transactions(farm_id);
CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions(type);
CREATE INDEX IF NOT EXISTS idx_transactions_transaction_date ON transactions(transaction_date);
CREATE INDEX IF NOT EXISTS idx_transactions_farm_date ON transactions(farm_id, transaction_date);
CREATE UNIQUE INDEX IF NOT EXISTS uk_transactions_type_reference_farm
    ON transactions(type, reference_id, farm_id);
