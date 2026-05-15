CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_farm_id ON users(farm_id);
CREATE INDEX IF NOT EXISTS idx_farms_name ON farms(name);

ALTER TABLE farms ADD CONSTRAINT fk_farms_master
    FOREIGN KEY (master_id) REFERENCES users(id) ON DELETE SET NULL;