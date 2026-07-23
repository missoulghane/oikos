-- =========================================================================
-- BILLING FEATURE: accounts (one per unit - a lot's own ledger - and one per
-- property, mirroring unit-side activity from the property's own
-- bookkeeping), their ledger (movements), the cotisation installments raised
-- against units, and the allocations (lettrage) linking credit movements to
-- installments.
-- All identifiers are UUIDs assigned application-side (never DB-generated).
-- account.holder_id is polymorphic (a unit or a property, per account_type)
-- so it carries no FK; installment.unit_id references unit(id) (an
-- installment is raised against a specific lot).
-- =========================================================================

CREATE TABLE account (
    id                  UUID PRIMARY KEY,
    holder_id           UUID NOT NULL,
    account_type        VARCHAR(20) NOT NULL,
    balance             NUMERIC(12, 2) NOT NULL DEFAULT 0,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_account_holder_id_account_type UNIQUE (holder_id, account_type)
);

-- Append-only ledger (RG002: never updated, never physically deleted).
CREATE TABLE movement (
    id                  UUID PRIMARY KEY,
    account_id          UUID NOT NULL,
    occurred_on         TIMESTAMPTZ NOT NULL,
    type                VARCHAR(30) NOT NULL,
    direction           VARCHAR(10) NOT NULL,
    amount              NUMERIC(12, 2) NOT NULL,
    label               VARCHAR(255) NOT NULL,
    business_reference  VARCHAR(255),
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_movement_account FOREIGN KEY (account_id) REFERENCES account (id)
);

CREATE INDEX idx_movement_account_id ON movement (account_id);

CREATE TABLE installment (
    id                  UUID PRIMARY KEY,
    account_id          UUID NOT NULL,
    unit_id             UUID NOT NULL,
    due_date            DATE NOT NULL,
    amount              NUMERIC(12, 2) NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_installment_account FOREIGN KEY (account_id) REFERENCES account (id),
    CONSTRAINT fk_installment_unit FOREIGN KEY (unit_id) REFERENCES unit (id)
);

CREATE INDEX idx_installment_account_id ON installment (account_id);
CREATE INDEX idx_installment_unit_id ON installment (unit_id);

-- Pure matching record (RG009): never alters movement or installment rows,
-- and unlike movement, may be physically deleted (RG012: deallocation).
CREATE TABLE allocation (
    id                  UUID PRIMARY KEY,
    movement_id         UUID NOT NULL,
    installment_id      UUID NOT NULL,
    allocated_amount    NUMERIC(12, 2) NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_allocation_movement FOREIGN KEY (movement_id) REFERENCES movement (id),
    CONSTRAINT fk_allocation_installment FOREIGN KEY (installment_id) REFERENCES installment (id)
);

CREATE INDEX idx_allocation_movement_id ON allocation (movement_id);
CREATE INDEX idx_allocation_installment_id ON allocation (installment_id);
