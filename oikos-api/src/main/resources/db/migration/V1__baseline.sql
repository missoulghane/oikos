-- =========================================================================
-- Baseline schema for the OIKOS application: party, user, auth, property
-- (property/building/unit type/unit, pricing, ownership and board) and
-- installment (cotisation calls) features.
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
    CONSTRAINT uk_party_email UNIQUE (email),
    CONSTRAINT uk_party_phone UNIQUE (phone)
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
-- 4. PROPERTY FEATURE: property, its buildings, their per-property unit type
-- catalog and units, per-property unit-type pricing, plus the two pivots
-- rattaching a party to the structure (SFD "Gestion de la Structure des
-- Coproprietes et des Acces").
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

-- Per-property, user-defined unit type catalog (e.g. "Appartement", "Box"),
-- always seeded with one default "OTHERS" row at property creation (see
-- CreatePropertyService/ConfigurePropertyService).
CREATE TABLE unit_type_definition (
    id                  UUID PRIMARY KEY,
    property_id         UUID NOT NULL,
    name                VARCHAR(50) NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_unit_type_definition_property FOREIGN KEY (property_id) REFERENCES property (id),
    CONSTRAINT uk_unit_type_definition_property_name UNIQUE (property_id, name)
);

CREATE INDEX idx_unit_type_definition_property_id ON unit_type_definition (property_id);

CREATE TABLE unit (
    id                  UUID PRIMARY KEY,
    building_id         UUID NOT NULL,
    unit_number         VARCHAR(20) NOT NULL,
    unit_type_id        UUID NOT NULL,
    shares              NUMERIC(12, 2) NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_unit_building FOREIGN KEY (building_id) REFERENCES building (id),
    CONSTRAINT fk_unit_unit_type FOREIGN KEY (unit_type_id) REFERENCES unit_type_definition (id)
);

CREATE INDEX idx_unit_building_id ON unit (building_id);
CREATE INDEX idx_unit_unit_type_id ON unit (unit_type_id);

-- Optional price per unit type for a given property (e.g. Appartement: 300,
-- Box: 100). At most one row per unit type; a unit type without a row simply
-- has no configured price (no default, no error). Removing a unit type
-- cascades to its price row.
CREATE TABLE unit_type_pricing (
    id                  UUID PRIMARY KEY,
    property_id         UUID NOT NULL,
    unit_type_id        UUID NOT NULL,
    price               NUMERIC(12, 2) NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_unit_type_pricing_property FOREIGN KEY (property_id) REFERENCES property (id),
    CONSTRAINT fk_unit_type_pricing_unit_type FOREIGN KEY (unit_type_id)
        REFERENCES unit_type_definition (id) ON DELETE CASCADE,
    CONSTRAINT uk_unit_type_pricing_unit_type UNIQUE (unit_type_id)
);

CREATE INDEX idx_unit_type_pricing_property_id ON unit_type_pricing (property_id);
CREATE INDEX idx_unit_type_pricing_unit_type_id ON unit_type_pricing (unit_type_id);

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
-- 5. INSTALLMENT FEATURE: cotisation calls and the installments they raise
-- against units. An installment call is a fund-collection event for a
-- property over one month (period); generating one raises an Installment
-- for every priced unit of the property. Unique (property_id, period)
-- prevents an accidental double call for the same month.
-- installment.installment_call_id is nullable: the manual
-- POST /installment-calls flow (arbitrary caller-supplied lines) does not
-- attach to a batch.
-- =========================================================================

CREATE TABLE installment_call (
    id                  UUID PRIMARY KEY,
    property_id         UUID NOT NULL,
    period              DATE NOT NULL,
    due_date            DATE NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_installment_call_property_period UNIQUE (property_id, period)
);

CREATE TABLE installment (
    id                  UUID PRIMARY KEY,
    unit_id             UUID NOT NULL,
    due_date            DATE NOT NULL,
    amount              NUMERIC(12, 2) NOT NULL,
    installment_call_id UUID,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_installment_unit FOREIGN KEY (unit_id) REFERENCES unit (id),
    CONSTRAINT fk_installment_installment_call FOREIGN KEY (installment_call_id) REFERENCES installment_call (id)
);

CREATE INDEX idx_installment_unit_id ON installment (unit_id);
CREATE INDEX idx_installment_installment_call_id ON installment (installment_call_id);

-- =========================================================================
-- 6. SEED / INITIAL DATA (Default Root Account)
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
