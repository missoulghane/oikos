-- Receipt numbering for owner payments.
--
-- payment.receipt_number is the human-readable reference printed on the PDF
-- (REC-2026-0042, see ReceiptNumber). Nullable on purpose: payments recorded
-- before this migration have none, and a receipt regenerated for one of them
-- allocates its number then. Once allocated it never changes - reprinting a
-- receipt must not renumber it.
ALTER TABLE payment ADD COLUMN receipt_number character varying(20);

-- One continuous series per (property, year), the usual accounting convention:
-- each copropriété keeps its own, and a receipt names its copropriété so two
-- properties sharing REC-2026-0001 is not ambiguous. The year is the payment's
-- value date, not the generation date.
--
-- Same shape and allocation strategy as sequence_piece (see
-- JournalEntryRepositoryAdapter.nextPieceNumber): a bare counter with no audit
-- columns, incremented under a pessimistic row lock so two concurrent payments
-- can never take the same number.
-- `receipt_year` and not `year`: the latter is a reserved word in H2 (the dev
-- profile's engine), which rejects it unquoted in the SQL Hibernate generates -
-- PostgreSQL accepts it, so the breakage would have been dev-only.
CREATE TABLE sequence_receipt (
    property_id uuid NOT NULL,
    receipt_year integer NOT NULL,
    next_number integer DEFAULT 1 NOT NULL
);

ALTER TABLE ONLY sequence_receipt
    ADD CONSTRAINT pk_sequence_receipt PRIMARY KEY (property_id, receipt_year);
