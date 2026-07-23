-- =========================================================================
-- INSTALLMENT CALL: a fund-collection event for a property over one month
-- (period). Generating one raises an Installment (+ its triggering debit
-- Movement) for every unit of the property, priced from UnitTypePricing.
-- Unique (property_id, period) prevents an accidental double call for the
-- same month. installment.installment_call_id is nullable: the pre-existing
-- manual POST /installment-calls flow (arbitrary caller-supplied lines) does
-- not attach to a batch.
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

ALTER TABLE installment
    ADD COLUMN installment_call_id UUID NULL,
    ADD CONSTRAINT fk_installment_installment_call FOREIGN KEY (installment_call_id) REFERENCES installment_call (id);

CREATE INDEX idx_installment_installment_call_id ON installment (installment_call_id);
