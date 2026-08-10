-- =========================================================================
-- V12: accounting_exercise (recreated - same shape as the deleted V3, kept
-- as-is per ADR 0001 decision "AccountingExercise existant, etendu"),
-- monthly period sub-entity (spec S4.1: "periodes mensuelles avec statut
-- propre"), piece-number sequence (I7), and the core double-entry ledger
-- (journal_entry/journal_entry_line, spec S4.1/S5).
-- =========================================================================

CREATE TABLE accounting_exercise (
    id                  UUID PRIMARY KEY,
    property_id         UUID NOT NULL REFERENCES property (id),
    label               VARCHAR(200) NOT NULL,
    start_date          DATE NOT NULL,
    end_date            DATE NOT NULL,
    status              VARCHAR(20) NOT NULL,
    closed_at           TIMESTAMPTZ,
    closed_by_user_id   UUID,
    comment             VARCHAR(1000),
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_accounting_exercise_property_id ON accounting_exercise (property_id);
-- Spec S3: a property has only one open exercise at a time.
CREATE UNIQUE INDEX uk_accounting_exercise_property_open ON accounting_exercise (property_id)
    WHERE status = 'OPEN';

CREATE TABLE period (
    id                  UUID PRIMARY KEY,
    exercise_id         UUID NOT NULL REFERENCES accounting_exercise (id),
    year_month          DATE NOT NULL,
    status              VARCHAR(20) NOT NULL,
    closed_at           TIMESTAMPTZ,
    closed_by_user_id   UUID,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_period_exercise_year_month ON period (exercise_id, year_month);

-- I7: continuous, gap-free numbering per (property, exercise, journal),
-- allocated under a row lock (SELECT ... FOR UPDATE in the Java use case).
CREATE TABLE sequence_piece (
    property_id     UUID NOT NULL REFERENCES property (id),
    exercise_id     UUID NOT NULL REFERENCES accounting_exercise (id),
    journal_code    VARCHAR(4) NOT NULL REFERENCES journal (code),
    next_number     INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (property_id, exercise_id, journal_code)
);

CREATE TABLE journal_entry (
    id                  UUID PRIMARY KEY,
    property_id         UUID NOT NULL REFERENCES property (id),
    exercise_id         UUID NOT NULL REFERENCES accounting_exercise (id),
    period_id           UUID NOT NULL REFERENCES period (id),
    journal_code        VARCHAR(4) NOT NULL REFERENCES journal (code),
    treasury_account_id UUID REFERENCES ledger_account (id),
    piece_date          DATE NOT NULL,
    piece_number        INTEGER,
    external_reference  VARCHAR(200),
    status              VARCHAR(20) NOT NULL,
    original_entry_id   UUID REFERENCES journal_entry (id),
    created_by_user_id  UUID NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0
);

-- piece_number is only allocated at validation time (I7) - null while
-- DRAFT, hence no NOT NULL constraint; unique once allocated.
CREATE UNIQUE INDEX uk_journal_entry_piece ON journal_entry (property_id, exercise_id, journal_code, piece_number)
    WHERE piece_number IS NOT NULL;
CREATE INDEX idx_journal_entry_property_date ON journal_entry (property_id, piece_date);
CREATE INDEX idx_journal_entry_original_entry ON journal_entry (original_entry_id);

CREATE TABLE journal_entry_line (
    id                  UUID PRIMARY KEY,
    journal_entry_id    UUID NOT NULL REFERENCES journal_entry (id),
    line_order          INTEGER NOT NULL,
    ledger_account_id   UUID NOT NULL REFERENCES ledger_account (id),
    auxiliary_unit_id   UUID REFERENCES unit (id),
    auxiliary_party_id  UUID REFERENCES party (id),
    direction           VARCHAR(10) NOT NULL,
    amount              NUMERIC(14, 2) NOT NULL CHECK (amount > 0),
    label               VARCHAR(200) NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_journal_entry_line_entry_id ON journal_entry_line (journal_entry_id);
CREATE INDEX idx_journal_entry_line_account_id ON journal_entry_line (ledger_account_id);
CREATE INDEX idx_journal_entry_line_auxiliary_unit ON journal_entry_line (auxiliary_unit_id);
CREATE INDEX idx_journal_entry_line_auxiliary_party ON journal_entry_line (auxiliary_party_id);

-- I4 (defense in depth - the use case layer is the primary enforcement):
-- a journal_entry is never physically deleted, and can only be updated
-- while DRAFT, except for the one legitimate DRAFT->POSTED transition
-- (which also allocates piece_number) and the one legitimate POSTED-
-- >REVERSED transition (P10, contre-passation) - lines are untouched by
-- either. journal_entry_line rows can never be inserted, updated or
-- deleted once their parent entry has left DRAFT.
CREATE OR REPLACE FUNCTION reject_journal_entry_mutation() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        RAISE EXCEPTION 'journal_entry % can never be physically deleted (I4/ADR 0001)', OLD.id;
    END IF;
    IF OLD.status = 'DRAFT' THEN
        RETURN NEW;
    END IF;
    IF OLD.status = 'POSTED' AND NEW.status = 'REVERSED' THEN
        RETURN NEW;
    END IF;
    RAISE EXCEPTION 'journal_entry % is not DRAFT and cannot be mutated except DRAFT->POSTED or POSTED->REVERSED (I4/ADR 0001)', OLD.id;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_journal_entry_immutable
    BEFORE UPDATE OR DELETE ON journal_entry
    FOR EACH ROW EXECUTE FUNCTION reject_journal_entry_mutation();

CREATE OR REPLACE FUNCTION reject_journal_entry_line_mutation_when_not_draft() RETURNS TRIGGER AS $$
DECLARE
    parent_status VARCHAR(20);
BEGIN
    SELECT status INTO parent_status FROM journal_entry WHERE id = COALESCE(NEW.journal_entry_id, OLD.journal_entry_id);
    IF parent_status IS DISTINCT FROM 'DRAFT' THEN
        RAISE EXCEPTION 'journal_entry_line for entry % cannot be inserted, updated or deleted once the entry left DRAFT (I4/ADR 0001)',
            COALESCE(NEW.journal_entry_id, OLD.journal_entry_id);
    END IF;
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_journal_entry_line_immutable
    BEFORE INSERT OR UPDATE OR DELETE ON journal_entry_line
    FOR EACH ROW EXECUTE FUNCTION reject_journal_entry_line_mutation_when_not_draft();
