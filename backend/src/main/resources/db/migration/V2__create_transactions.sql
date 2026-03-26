CREATE TYPE category_type AS ENUM (
    'DINING', 'GROCERIES', 'TRANSPORT', 'ENTERTAINMENT',
    'UTILITIES', 'HEALTH', 'SHOPPING', 'OTHER'
);

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    amount DECIMAL(10,2) NOT NULL,
    category category_type NOT NULL,
    description VARCHAR(500),
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
