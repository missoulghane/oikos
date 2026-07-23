-- =========================================================================
-- Baseline schema for the OIKOS application: party, user, auth and
-- property (property/building/unit, ownership and board) features.
-- All identifiers are UUIDs assigned application-side (never DB-generated).
-- =========================================================================

-- =========================================================================
-- 1. PARTY FEATURE
-- Identity record of a legal actor (individual or company), independent of
-- any application account. app_user, unit_ownership and board_member all
-- reference party by id.
-- =========================================================================

CREATE TABLE party (
    id                  UUID PRIMARY KEY,
    full_name           VARCHAR(200) NOT NULL,
    party_type          VARCHAR(20) NOT NULL,
    email               VARCHAR(150) NOT NULL,
    phone               VARCHAR(20),
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_party_email UNIQUE (email)
);

-- =========================================================================
-- 2. USER FEATURE TABLES
-- app_user carries only login credentials: identity (name/email) lives on
-- the party it references.
-- =========================================================================

CREATE TABLE app_user (
    id                  UUID PRIMARY KEY,
    party_id            UUID NOT NULL,
    login               VARCHAR(150),
    password_hash       VARCHAR(255) NOT NULL,
    verified            BOOLEAN NOT NULL DEFAULT FALSE,
    enabled             BOOLEAN NOT NULL DEFAULT TRUE,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_app_user_login UNIQUE (login),
    CONSTRAINT fk_app_user_party FOREIGN KEY (party_id) REFERENCES party (id)
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
-- 3. AUTH FEATURE TABLES
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
-- 4. PROPERTY FEATURE: property, its buildings and their units, plus the
-- two pivots rattaching a party to the structure (SFD "Gestion de la
-- Structure des Coproprietes et des Acces").
-- =========================================================================

CREATE TABLE property (
    id                  UUID PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    address             VARCHAR(250) NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE building (
    id                  UUID PRIMARY KEY,
    property_id         UUID NOT NULL,
    name                VARCHAR(100) NOT NULL,
    floor_count         INTEGER NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_building_property FOREIGN KEY (property_id) REFERENCES property (id)
);

CREATE INDEX idx_building_property_id ON building (property_id);

CREATE TABLE unit (
    id                  UUID PRIMARY KEY,
    building_id         UUID NOT NULL,
    unit_number         VARCHAR(20) NOT NULL,
    unit_type           VARCHAR(20) NOT NULL,
    shares              NUMERIC(12, 2) NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_unit_building FOREIGN KEY (building_id) REFERENCES building (id)
);

CREATE INDEX idx_unit_building_id ON unit (building_id);

CREATE TABLE unit_ownership (
    id                  UUID PRIMARY KEY,
    unit_id             UUID NOT NULL,
    party_id            UUID NOT NULL,
    ownership_share     NUMERIC(5, 2) NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_unit_ownership_unit FOREIGN KEY (unit_id) REFERENCES unit (id),
    CONSTRAINT fk_unit_ownership_party FOREIGN KEY (party_id) REFERENCES party (id),
    CONSTRAINT uk_unit_ownership_unit_party UNIQUE (unit_id, party_id)
);

CREATE INDEX idx_unit_ownership_unit_id ON unit_ownership (unit_id);
CREATE INDEX idx_unit_ownership_party_id ON unit_ownership (party_id);

CREATE TABLE board_member (
    id                  UUID PRIMARY KEY,
    property_id         UUID NOT NULL,
    party_id            UUID NOT NULL,
    board_role          VARCHAR(30) NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_board_member_property FOREIGN KEY (property_id) REFERENCES property (id),
    CONSTRAINT fk_board_member_party FOREIGN KEY (party_id) REFERENCES party (id),
    CONSTRAINT uk_board_member_property_party_role UNIQUE (property_id, party_id, board_role)
);

CREATE INDEX idx_board_member_property_id ON board_member (property_id);
CREATE INDEX idx_board_member_party_id ON board_member (party_id);

-- =========================================================================
-- 5. SEED / INITIAL DATA (Default Root Account)
-- =========================================================================

INSERT INTO party (id, full_name, party_type, email, phone, created_date, last_modified_date, version)
VALUES (
    '402888b2-2370-4c5e-aba6-985da776bb17',
    'Root IAM',
    'INDIVIDUAL',
    'admin@oikos.com',
    NULL,
    NOW(),
    NOW(),
    0
)
ON CONFLICT (email) DO NOTHING;

-- Password hash below is BCrypt("iam@root/2026"), generated with the same
-- algorithm/strength as com.architek.oikos.shared.infrastructure.configuration.PasswordEncoderConfiguration.
INSERT INTO app_user (id, party_id, login, password_hash, verified, enabled, created_date, last_modified_date, version)
VALUES (
    '402888b2-2370-4c5e-aba6-985da776bb17',
    '402888b2-2370-4c5e-aba6-985da776bb17',
    NULL,
    '$2y$10$lX.1MG7sstLRQVOXXF0SruxPiT.USXqTBZqmHAz.OO1dCZ9RTSMEO',
    TRUE,
    TRUE,
    NOW(),
    NOW(),
    0
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_role (user_id, role)
VALUES ('402888b2-2370-4c5e-aba6-985da776bb17', 'ROLE_MASTER'),
       ('402888b2-2370-4c5e-aba6-985da776bb17', 'ROLE_ADMIN')
ON CONFLICT DO NOTHING;
