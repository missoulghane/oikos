-- =========================================================================
-- V14: owner payments (P2/P3), generic allocation/lettrage (I8, links two
-- journal_entry_line rows - deliberately auxiliary-agnostic so the same
-- mechanism serves both unit receivable<->payment matching, P2/P3, and
-- supplier invoice<->payment matching, P5, without two parallel lettrage
-- engines), and supplier invoices (P4). No dedicated table for supplier
-- settlements (P5) or staff payroll (P6/P7): both post directly through
-- journal_entry/journal_entry_line plus, for P5, an allocation row - see
-- ADR 0001.
-- =========================================================================

CREATE TABLE payment (
    id                  UUID PRIMARY KEY,
    property_id         UUID NOT NULL REFERENCES property (id),
    unit_id             UUID NOT NULL REFERENCES unit (id),
    mode                VARCHAR(20) NOT NULL,
    value_date          DATE NOT NULL,
    amount              NUMERIC(14, 2) NOT NULL CHECK (amount > 0),
    journal_entry_id    UUID NOT NULL REFERENCES journal_entry (id),
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_payment_unit_id ON payment (unit_id);
CREATE INDEX idx_payment_property_id ON payment (property_id);

-- I8: a debit line is never allocated beyond its own remaining amount -
-- enforced by the use case (sum of allocations per debit_line_id <=
-- the line's amount), not by a DB constraint (would need an aggregate
-- check across rows).
CREATE TABLE allocation (
    id                  UUID PRIMARY KEY,
    debit_line_id       UUID NOT NULL REFERENCES journal_entry_line (id),
    credit_line_id      UUID NOT NULL REFERENCES journal_entry_line (id),
    amount              NUMERIC(14, 2) NOT NULL CHECK (amount > 0),
    allocated_date      DATE NOT NULL,
    allocated_by_user_id UUID NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_allocation_debit_line ON allocation (debit_line_id);
CREATE INDEX idx_allocation_credit_line ON allocation (credit_line_id);

-- Allocations are append-only (RG spec S4.1: "toujours produites ...,
-- jamais modifiees") but can be physically removed on de-allocation
-- (RG012-equivalent) - no immutability trigger here, unlike journal_entry.

CREATE TABLE expense (
    id                  UUID PRIMARY KEY,
    property_id         UUID NOT NULL REFERENCES property (id),
    date                DATE NOT NULL,
    supplier_party_id   UUID NOT NULL REFERENCES party (id),
    ledger_account_id   UUID NOT NULL REFERENCES ledger_account (id),
    amount              NUMERIC(14, 2) NOT NULL CHECK (amount > 0),
    description         VARCHAR(1000),
    receipt_reference   VARCHAR(200),
    journal_entry_id    UUID NOT NULL REFERENCES journal_entry (id),
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_expense_property_id ON expense (property_id);
CREATE INDEX idx_expense_supplier_party_id ON expense (supplier_party_id);
