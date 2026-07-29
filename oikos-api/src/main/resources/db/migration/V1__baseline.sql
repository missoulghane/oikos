-- =========================================================================
-- Baseline schema for the OIKOS application: property (properties, buildings,
-- unit types, units, pricing, ownership and board), party (identity of a
-- legal actor, scoped to one property), user/auth (platform accounts,
-- independent of any property) and installment (cotisation calls) features.
-- All identifiers are UUIDs assigned application-side (never DB-generated).
-- =========================================================================

-- =========================================================================
-- 1. PROPERTY FEATURE (root): property, its buildings and its per-property
-- unit type catalog. Units and the two pivots rattaching a party to the
-- structure (unit_ownership, board_member) come later, once Party (which
-- they reference) is defined.
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

-- =========================================================================
-- 2. PARTY FEATURE
-- Identity record of a legal actor (individual or company), scoped to
-- exactly one property (tenant boundary): the same real person owning units
-- or sitting on boards in two different properties is represented by two
-- distinct Party rows, one per property. unit_ownership and board_member
-- both reference party by id, always within the same property (enforced by
-- composite FKs below). A Party may optionally be linked to at most one
-- AppUser (app_user_party), independent of any application account.
-- =========================================================================

CREATE TABLE party (
    id                  UUID PRIMARY KEY,
    property_id         UUID NOT NULL,
    full_name           VARCHAR(200) NOT NULL,
    party_type          VARCHAR(20) NOT NULL,
    email               VARCHAR(150) NOT NULL,
    phone               VARCHAR(20),
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_party_property FOREIGN KEY (property_id) REFERENCES property (id),
    CONSTRAINT uk_party_property_email UNIQUE (property_id, email),
    -- Composite-FK target for unit_ownership/board_member: guarantees a
    -- party can only be referenced together with the property_id it
    -- actually belongs to.
    CONSTRAINT uk_party_id_property UNIQUE (id, property_id)
);

CREATE INDEX idx_party_property_id ON party (property_id);
CREATE UNIQUE INDEX uk_party_property_phone ON party (property_id, phone) WHERE phone IS NOT NULL;

-- =========================================================================
-- 3. PROPERTY FEATURE (continued): units (now that unit_type_definition
-- exists) and the two pivots rattaching a party to the structure (SFD
-- "Gestion de la Structure des Coproprietes et des Acces"), now that Party
-- exists.
-- =========================================================================

CREATE TABLE unit (
    id                  UUID PRIMARY KEY,
    building_id         UUID NOT NULL,
    property_id         UUID NOT NULL,
    unit_number         VARCHAR(20) NOT NULL,
    unit_type_id        UUID NOT NULL,
    shares              NUMERIC(12, 2) NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_unit_building FOREIGN KEY (building_id) REFERENCES building (id),
    CONSTRAINT fk_unit_property FOREIGN KEY (property_id) REFERENCES property (id),
    CONSTRAINT fk_unit_unit_type FOREIGN KEY (unit_type_id) REFERENCES unit_type_definition (id),
    -- Composite-FK target for unit_ownership: guarantees a unit can only be
    -- referenced together with the property_id it actually belongs to.
    CONSTRAINT uk_unit_id_property UNIQUE (id, property_id)
);

CREATE INDEX idx_unit_building_id ON unit (building_id);
CREATE INDEX idx_unit_property_id ON unit (property_id);
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

-- unit_id and party_id are each paired with property_id via composite FKs,
-- so a unit and its owner are guaranteed to belong to the same property -
-- the DB rejects any cross-property ownership row outright.
CREATE TABLE unit_ownership (
    id                  UUID PRIMARY KEY,
    unit_id             UUID NOT NULL,
    party_id            UUID NOT NULL,
    property_id         UUID NOT NULL,
    ownership_share     NUMERIC(5, 2) NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_unit_ownership_unit_party UNIQUE (unit_id, party_id),
    CONSTRAINT fk_unit_ownership_unit_property FOREIGN KEY (unit_id, property_id) REFERENCES unit (id, property_id),
    CONSTRAINT fk_unit_ownership_party_property FOREIGN KEY (party_id, property_id) REFERENCES party (id, property_id)
);

CREATE INDEX idx_unit_ownership_unit_id ON unit_ownership (unit_id);
CREATE INDEX idx_unit_ownership_party_id ON unit_ownership (party_id);
CREATE INDEX idx_unit_ownership_property_id ON unit_ownership (property_id);

-- party_id is paired with property_id via a composite FK, so a board member
-- is guaranteed to belong to the same property they sit on the board of.
CREATE TABLE board_member (
    id                  UUID PRIMARY KEY,
    property_id         UUID NOT NULL,
    party_id            UUID NOT NULL,
    board_role          VARCHAR(30) NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_board_member_property FOREIGN KEY (property_id) REFERENCES property (id),
    CONSTRAINT fk_board_member_party_property FOREIGN KEY (party_id, property_id) REFERENCES party (id, property_id),
    CONSTRAINT uk_board_member_property_party_role UNIQUE (property_id, party_id, board_role)
);

CREATE INDEX idx_board_member_property_id ON board_member (property_id);
CREATE INDEX idx_board_member_party_id ON board_member (party_id);

-- =========================================================================
-- 4. USER FEATURE TABLES
-- app_user is a fully standalone platform account (its own email/full_name),
-- independent of any Party. It may optionally be linked to any number of
-- per-property Party rows via app_user_party (at most one AppUser per
-- Party), each carrying its own set of property-scoped roles via
-- app_user_party_role. Platform-wide roles (ROLE_USER, ROLE_ADMIN,
-- ROLE_MASTER) live directly on app_user via user_role.
-- =========================================================================

CREATE TABLE app_user (
    id                  UUID PRIMARY KEY,
    email               VARCHAR(150) NOT NULL,
    full_name           VARCHAR(200) NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    verified            BOOLEAN NOT NULL DEFAULT FALSE,
    enabled             BOOLEAN NOT NULL DEFAULT TRUE,
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

-- A Party links to at most one AppUser (uk_app_user_party_party); an
-- AppUser may link to any number of Party rows across different properties.
CREATE TABLE app_user_party (
    app_user_id UUID NOT NULL,
    party_id    UUID NOT NULL,
    CONSTRAINT pk_app_user_party PRIMARY KEY (app_user_id, party_id),
    CONSTRAINT uk_app_user_party_party UNIQUE (party_id),
    CONSTRAINT fk_app_user_party_user FOREIGN KEY (app_user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_app_user_party_party FOREIGN KEY (party_id) REFERENCES party (id) ON DELETE CASCADE
);

-- Per-property roles (e.g. ROLE_PROPERTY_MANAGER, ROLE_PROPERTY_ADMIN),
-- granted to an AppUser through one of its linked Party rows. property_id is
-- denormalized from that party's own property_id (composite FK below
-- guarantees they always agree) so authorization checks can filter an
-- AppUser's grants by property without reloading each referenced Party.
CREATE TABLE app_user_party_role (
    app_user_id UUID NOT NULL,
    party_id    UUID NOT NULL,
    property_id UUID NOT NULL,
    role        VARCHAR(30) NOT NULL,
    CONSTRAINT pk_app_user_party_role PRIMARY KEY (app_user_id, party_id, role),
    CONSTRAINT fk_app_user_party_role_user FOREIGN KEY (app_user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_app_user_party_role_party_property FOREIGN KEY (party_id, property_id) REFERENCES party (id, property_id) ON DELETE CASCADE
);

CREATE INDEX idx_app_user_party_role_property_id ON app_user_party_role (property_id);

-- Single-use invitation linking an owner's Party to an AppUser account
-- (created new, or matched by email to an existing one) - see
-- InvitePartyService/AcceptPartyInvitationService. Keyed by party_id rather
-- than user_id since no AppUser may exist yet at issuance time.
CREATE TABLE party_invitation_token (
    id                  UUID PRIMARY KEY,
    party_id            UUID NOT NULL,
    email               VARCHAR(150) NOT NULL,
    full_name           VARCHAR(200) NOT NULL,
    token               VARCHAR(255) NOT NULL,
    expires_at          TIMESTAMPTZ NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_party_invitation_token_token UNIQUE (token),
    CONSTRAINT fk_party_invitation_token_party FOREIGN KEY (party_id) REFERENCES party (id) ON DELETE CASCADE
);

CREATE INDEX idx_party_invitation_token_party_id ON party_invitation_token (party_id);

-- =========================================================================
-- 5. AUTH FEATURE TABLES
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
-- 6. INSTALLMENT FEATURE: cotisation calls and the installments they raise
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
-- 7. SEED / INITIAL DATA (Default Root Account)
-- Platform-wide account, not tied to any property: no Party needed.
-- =========================================================================

-- Password hash below is BCrypt("iam@root/2026"), generated with the same
-- algorithm/strength as com.architek.oikos.shared.infrastructure.configuration.PasswordEncoderConfiguration.
INSERT INTO app_user (id, email, full_name, password_hash, verified, enabled, created_date, last_modified_date, version)
VALUES (
    '402888b2-2370-4c5e-aba6-985da776bb17',
    'admin@oikos.com',
    'Root IAM',
    '$2y$10$lX.1MG7sstLRQVOXXF0SruxPiT.USXqTBZqmHAz.OO1dCZ9RTSMEO',
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
