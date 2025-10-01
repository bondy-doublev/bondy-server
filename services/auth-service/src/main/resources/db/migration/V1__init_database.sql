-- USERS
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    first_name TEXT NOT NULL,
    middle_name TEXT NULL,
    last_name TEXT NOT NULL,
    avatar_url TEXT NULL,
    dob TIMESTAMP NULL,
    gender BOOLEAN NULL,
    role VARCHAR(10) NOT NULL DEFAULT 'USER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NULL,
    CONSTRAINT ck_users_role CHECK (role IN ('USER','ADMIN'))
);

-- PRE_REGISTRATIONS
CREATE TABLE pre_registrations (
    id BIGSERIAL PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    first_name TEXT NOT NULL,
    middle_name TEXT NULL,
    last_name TEXT NOT NULL,
    dob TIMESTAMP NOT NULL,
    gender BOOLEAN NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- ACCOUNTS
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    password_hash TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_accounts_users
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- REFRESH TOKENS
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash TEXT NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_refreshtokens_users
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- OTP CODES
CREATE TABLE otp_codes (
    id BIGSERIAL PRIMARY KEY,
    subject_type VARCHAR(20) NOT NULL,
    subject_id BIGINT NOT NULL,
    purpose VARCHAR(20) NOT NULL,
    code_hash TEXT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- API KEYS
CREATE TABLE api_keys (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    key_hash CHAR(64) NOT NULL UNIQUE,
    prefix   VARCHAR(12) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NULL,
    active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_active_otp_per_pre_reg
    ON otp_codes(subject_id, purpose)
    WHERE active = TRUE;

CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_refreshtokens_user_id ON refresh_tokens(user_id);

CREATE INDEX idx_api_keys_active ON api_keys(active, expires_at);
CREATE INDEX idx_api_keys_prefix ON api_keys(prefix);