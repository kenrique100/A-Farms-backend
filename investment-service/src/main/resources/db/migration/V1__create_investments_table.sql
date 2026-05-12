CREATE TABLE IF NOT EXISTS investments (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    occurred_at DATE NOT NULL
);
