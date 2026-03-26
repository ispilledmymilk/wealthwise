CREATE TABLE budgets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    category category_type NOT NULL,
    monthly_limit DECIMAL(10,2) NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    UNIQUE(user_id, category, month, year)
);
