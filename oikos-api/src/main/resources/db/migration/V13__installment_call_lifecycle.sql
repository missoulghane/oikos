-- =========================================================================
-- V13: appel de fonds lifecycle (spec S4.1/P1: DRAFT -> ISSUED -> POSTED ->
-- CANCELLED) and its link to the journal_entry generated at
-- "comptabilisation". installment_call/installment already have the right
-- shape (S4.1: an Installment is already a "ligne d'appel" per lot) - this
-- migration only extends them, per ADR 0001.
--
-- Backfilled conservatively to POSTED (installment rows already carry real
-- amounts/outstanding_amount today, i.e. they behave as already-posted
-- calls) rather than a status implying no financial impact yet.
-- =========================================================================

ALTER TABLE installment_call ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'POSTED';
ALTER TABLE installment_call ADD COLUMN journal_entry_id UUID REFERENCES journal_entry (id);
ALTER TABLE installment_call ALTER COLUMN status DROP DEFAULT;

-- Links the debit line raised for this installment (P1) to its journal
-- entry line, replacing the old UnitAccountMovement.businessReference
-- string-based back-reference (ADR 0001 S4.3 anti-derivation principle:
-- first-class identifiers, never string-derived).
ALTER TABLE installment ADD COLUMN journal_entry_line_id UUID REFERENCES journal_entry_line (id);
