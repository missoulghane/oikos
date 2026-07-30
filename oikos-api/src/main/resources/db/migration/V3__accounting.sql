-- =========================================================================
-- V3: accounting module (Phase 1) - gestion comptable simplifiee
-- copropriete. Two axes: la tresorerie reelle de la propriete
-- (financial_account/financial_journal_entry/expense) and the per-lot
-- customer account (unit_account/unit_account_movement), scoped to a
-- property's accounting_exercise. FK to property/unit/installment are real
-- (cross-module FKs at the DB level are already the norm - see
-- installment.unit_id -> unit.id in V1); the hexagonal boundary is enforced
-- in the Java code (ports only), never in the schema.
--
-- Explicitly out of scope for this migration (Phase 2, later):
-- fine-grained payment-to-installment allocation/lettrage, and exercise
-- closing (closing balances + "Report a nouveau" carry-forward entries) -
-- accounting_exercise.status/closed_at/closed_by_user_id columns exist so
-- the shape is ready, but no code transitions a row to CLOSED yet.
-- =========================================================================

CREATE TABLE accounting_exercise (
    id                  UUID PRIMARY KEY,
    property_id         UUID NOT NULL,
    label               VARCHAR(200) NOT NULL,
    start_date          DATE NOT NULL,
    end_date            DATE NOT NULL,
    status              VARCHAR(20) NOT NULL,
    closed_at           TIMESTAMPTZ,
    closed_by_user_id   UUID,
    comment             VARCHAR(1000),
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_accounting_exercise_property FOREIGN KEY (property_id) REFERENCES property (id)
);

CREATE INDEX idx_accounting_exercise_property_id ON accounting_exercise (property_id);
-- Spec RG (section 3): a property has only one open exercise at a time.
CREATE UNIQUE INDEX uk_accounting_exercise_property_open ON accounting_exercise (property_id)
    WHERE status = 'OPEN';

CREATE TABLE financial_account (
    id                  UUID PRIMARY KEY,
    property_id         UUID NOT NULL,
    name                VARCHAR(200) NOT NULL,
    type                VARCHAR(20) NOT NULL,
    currency            VARCHAR(10) NOT NULL,
    balance             NUMERIC(12, 2) NOT NULL,
    status              VARCHAR(20) NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_financial_account_property FOREIGN KEY (property_id) REFERENCES property (id)
);

CREATE INDEX idx_financial_account_property_id ON financial_account (property_id);

CREATE TABLE financial_journal_entry (
    id                  UUID PRIMARY KEY,
    exercise_id         UUID NOT NULL,
    financial_account_id UUID NOT NULL,
    date                DATE NOT NULL,
    type                VARCHAR(30) NOT NULL,
    direction           VARCHAR(10) NOT NULL,
    amount              NUMERIC(12, 2) NOT NULL,
    label               VARCHAR(200) NOT NULL,
    business_reference  VARCHAR(200),
    created_by_user_id  UUID NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_financial_journal_entry_exercise FOREIGN KEY (exercise_id) REFERENCES accounting_exercise (id),
    CONSTRAINT fk_financial_journal_entry_account FOREIGN KEY (financial_account_id) REFERENCES financial_account (id)
);

CREATE INDEX idx_financial_journal_entry_account_id ON financial_journal_entry (financial_account_id);
CREATE INDEX idx_financial_journal_entry_exercise_id ON financial_journal_entry (exercise_id);

CREATE TABLE expense (
    id                  UUID PRIMARY KEY,
    exercise_id         UUID NOT NULL,
    financial_account_id UUID NOT NULL,
    date                DATE NOT NULL,
    category            VARCHAR(100) NOT NULL,
    provider            VARCHAR(200) NOT NULL,
    amount              NUMERIC(12, 2) NOT NULL,
    description         VARCHAR(1000),
    receipt_reference   VARCHAR(200),
    journal_entry_id    UUID NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_expense_exercise FOREIGN KEY (exercise_id) REFERENCES accounting_exercise (id),
    CONSTRAINT fk_expense_account FOREIGN KEY (financial_account_id) REFERENCES financial_account (id),
    CONSTRAINT fk_expense_journal_entry FOREIGN KEY (journal_entry_id) REFERENCES financial_journal_entry (id)
);

CREATE INDEX idx_expense_account_id ON expense (financial_account_id);

CREATE TABLE unit_account (
    id                  UUID PRIMARY KEY,
    unit_id             UUID NOT NULL,
    property_id         UUID NOT NULL,
    balance             NUMERIC(12, 2) NOT NULL,
    last_updated_date   TIMESTAMPTZ NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_unit_account_unit_id UNIQUE (unit_id),
    CONSTRAINT fk_unit_account_unit FOREIGN KEY (unit_id) REFERENCES unit (id),
    CONSTRAINT fk_unit_account_property FOREIGN KEY (property_id) REFERENCES property (id)
);

CREATE INDEX idx_unit_account_property_id ON unit_account (property_id);

CREATE TABLE unit_account_movement (
    id                  UUID PRIMARY KEY,
    exercise_id         UUID NOT NULL,
    unit_account_id     UUID NOT NULL,
    date                DATE NOT NULL,
    type                VARCHAR(20) NOT NULL,
    direction           VARCHAR(10) NOT NULL,
    amount              NUMERIC(12, 2) NOT NULL,
    business_reference  VARCHAR(200),
    label               VARCHAR(200) NOT NULL,
    reason              VARCHAR(500),
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_unit_account_movement_exercise FOREIGN KEY (exercise_id) REFERENCES accounting_exercise (id),
    CONSTRAINT fk_unit_account_movement_account FOREIGN KEY (unit_account_id) REFERENCES unit_account (id)
);

CREATE INDEX idx_unit_account_movement_account_id ON unit_account_movement (unit_account_id);
