-- =========================================================================
-- Baseline schema for the OIKOS module: user + auth features.
-- All identifiers are UUIDs assigned application-side (never DB-generated).
-- =========================================================================

-- =========================================================================
-- 1. USER FEATURE TABLES
-- =========================================================================

CREATE TABLE app_user (
    id                  UUID PRIMARY KEY,
    email               VARCHAR(255) NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    verified            BOOLEAN NOT NULL DEFAULT FALSE,
    enabled             BOOLEAN NOT NULL DEFAULT TRUE, -- Integrated directly from alter table script
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_app_user_email UNIQUE (email)
);

CREATE TABLE user_role (
    user_id UUID NOT NULL,
    role    VARCHAR(50) NOT NULL,
    CONSTRAINT pk_user_role PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);

CREATE TABLE verification_token (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL,
    token               VARCHAR(255) NOT NULL,
    expires_at          TIMESTAMPTZ NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_verification_token_token UNIQUE (token),
    CONSTRAINT fk_verification_token_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);

CREATE INDEX idx_verification_token_user_id ON verification_token (user_id);

-- =========================================================================
-- 2. AUTH FEATURE TABLES
-- =========================================================================

CREATE TABLE refresh_token (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL,
    token_hash          VARCHAR(255) NOT NULL,
    expires_at          TIMESTAMPTZ NOT NULL,
    revoked             BOOLEAN NOT NULL DEFAULT FALSE,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_refresh_token_token_hash UNIQUE (token_hash)
);

CREATE INDEX idx_refresh_token_user_id ON refresh_token (user_id);

CREATE TABLE refresh_token_authority (
    refresh_token_id UUID NOT NULL,
    authority        VARCHAR(50) NOT NULL,
    CONSTRAINT pk_refresh_token_authority PRIMARY KEY (refresh_token_id, authority),
    CONSTRAINT fk_refresh_token_authority_token FOREIGN KEY (refresh_token_id) REFERENCES refresh_token (id) ON DELETE CASCADE
);

-- =========================================================================
-- AUTH FEATURE: PASSWORD-RESET FLOW
-- =========================================================================

CREATE TABLE password_reset_token (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL,
    token               VARCHAR(255) NOT NULL,
    expires_at          TIMESTAMPTZ NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_password_reset_token_token UNIQUE (token),
    CONSTRAINT fk_password_reset_token_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_token_user_id ON password_reset_token (user_id);

-- =========================================================================
-- 3. SEED / INITIAL DATA (Default Root Account)
-- =========================================================================

-- Password hash below is BCrypt("iam@root/2026"), generated with the same
-- algorithm/strength as com.architek.oikos.shared.infrastructure.configuration.PasswordEncoderConfiguration.
INSERT INTO app_user (id, email, password_hash, first_name, last_name, verified, enabled, created_date, last_modified_date, version)
VALUES (
    '402888b2-2370-4c5e-aba6-985da776bb17',
    'admin@oikos.com',
    '$2y$10$lX.1MG7sstLRQVOXXF0SruxPiT.USXqTBZqmHAz.OO1dCZ9RTSMEO',
    'IAM',
    'Root',
    TRUE,
    TRUE,
    NOW(),
    NOW(),
    0
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_role (user_id, role)
VALUES ('402888b2-2370-4c5e-aba6-985da776bb17', 'ROLE_MASTER'),
       ('402888b2-2370-4c5e-aba6-985da776bb17', 'ROLE_ADMIN')
ON CONFLICT DO NOTHING;