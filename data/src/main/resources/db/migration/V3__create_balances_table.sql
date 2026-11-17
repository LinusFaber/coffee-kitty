SET TIME ZONE 'UTC';

CREATE TABLE balances (
    id uuid NOT NULL PRIMARY KEY,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL,
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    balance numeric(12, 2) NOT NULL DEFAULT 0.00 CHECK (balance >= 0),
    CONSTRAINT uq_balances_user UNIQUE (user_id)
);

INSERT INTO balances (id, created_at, updated_at, user_id, balance)
SELECT gen_random_uuid(), now(), now(), id, 0.00 FROM users;
