-- =========================================================================
-- V4: lettrage - fine-grained payment-to-fund-call allocation, explicitly
-- deferred as "Phase 2" in V3's own comment. One row = "this much of this
-- credit movement (payment or credit regularization) settles this much of
-- that debit movement (fund call)". Append-only, produced exclusively by
-- LettrageProposalCalculator - never entered freely.
-- =========================================================================

CREATE TABLE unit_account_allocation (
    id                   UUID PRIMARY KEY,
    unit_account_id      UUID NOT NULL,
    debit_movement_id    UUID NOT NULL,
    credit_movement_id   UUID NOT NULL,
    amount               NUMERIC(12, 2) NOT NULL,
    allocated_date       DATE NOT NULL,
    allocated_by_user_id UUID NOT NULL,
    created_date         TIMESTAMPTZ NOT NULL,
    last_modified_date   TIMESTAMPTZ NOT NULL,
    version              BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_unit_account_allocation_account FOREIGN KEY (unit_account_id) REFERENCES unit_account (id),
    CONSTRAINT fk_unit_account_allocation_debit FOREIGN KEY (debit_movement_id) REFERENCES unit_account_movement (id),
    CONSTRAINT fk_unit_account_allocation_credit FOREIGN KEY (credit_movement_id) REFERENCES unit_account_movement (id)
);

CREATE INDEX idx_unit_account_allocation_account_id ON unit_account_allocation (unit_account_id);
