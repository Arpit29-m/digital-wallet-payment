-- V1__Initial_Schema.sql
-- Initial schema for Digital Wallet API
-- Run order enforced by Flyway version prefix

-- -----------------------------------------------------------------------
-- Lookup / reference tables first (no FK dependencies)
-- -----------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS roles (
    id          BIG SERIAL PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- -----------------------------------------------------------------------
-- Core user table
-- -----------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    first_name    VARCHAR(50)  NOT NULL,
    last_name     VARCHAR(50)  NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    phone_number  VARCHAR(20)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status        VARCHAR(30)  NOT NULL DEFAULT 'PENDING_VERIFICATION',
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE','INACTIVE','SUSPENDED','PENDING_VERIFICATION'))
);

CREATE INDEX idx_users_email  ON users(email);
CREATE INDEX idx_users_phone  ON users(phone_number);

-- -----------------------------------------------------------------------
-- Many-to-many: users <-> roles
-- -----------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- -----------------------------------------------------------------------
-- Wallet table
-- -----------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS wallets (
    id            BIGSERIAL PRIMARY KEY,
    wallet_number VARCHAR(20)     NOT NULL UNIQUE,
    balance       NUMERIC(19, 4)  NOT NULL DEFAULT 0.0000,
    currency      VARCHAR(3)      NOT NULL DEFAULT 'USD',
    status        VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    owner_id      BIGINT          NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at    TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_wallets_status  CHECK (status IN ('ACTIVE','FROZEN','CLOSED')),
    CONSTRAINT chk_wallets_balance CHECK (balance >= 0)
);

CREATE INDEX idx_wallets_wallet_number ON wallets(wallet_number);
CREATE INDEX idx_wallets_owner_id      ON wallets(owner_id);

-- -----------------------------------------------------------------------
-- Transactions table
-- -----------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS transactions (
    id                      BIGSERIAL PRIMARY KEY,
    reference               VARCHAR(50)     NOT NULL UNIQUE,
    amount                  NUMERIC(19, 4)  NOT NULL,
    currency                VARCHAR(3)      NOT NULL,
    type                    VARCHAR(20)     NOT NULL,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    source_wallet_id        BIGINT          REFERENCES wallets(id) ON DELETE RESTRICT,
    destination_wallet_id   BIGINT          REFERENCES wallets(id) ON DELETE RESTRICT,
    description             VARCHAR(255),
    reversal_transaction_id BIGINT          REFERENCES transactions(id),
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_tx_type   CHECK (type IN ('DEPOSIT','WITHDRAWAL','TRANSFER','RECEIVE','REFUND')),
    CONSTRAINT chk_tx_status CHECK (status IN ('PENDING','COMPLETED','FAILED','REVERSED')),
    CONSTRAINT chk_tx_amount CHECK (amount > 0)
);

CREATE INDEX idx_tx_reference   ON transactions(reference);
CREATE INDEX idx_tx_source      ON transactions(source_wallet_id);
CREATE INDEX idx_tx_destination ON transactions(destination_wallet_id);
CREATE INDEX idx_tx_status      ON transactions(status);

-- -----------------------------------------------------------------------
-- Seed default roles
-- -----------------------------------------------------------------------

INSERT INTO roles (name, description) VALUES
    ('ROLE_USER',  'Standard wallet user'),
    ('ROLE_ADMIN', 'Platform administrator')
ON CONFLICT (name) DO NOTHING;
