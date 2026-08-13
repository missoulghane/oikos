-- Populates the `journal` reference table (see JournalCode.java for the
-- closed, engine-defined catalog of the 6 journals this mirrors - fixed by
-- spec §3.3, no CRUD in the API surface). Missing until now, same class of
-- gap as V2__seed_permission_catalog.sql: journal_entry.journal_code and
-- sequence_piece.journal_code both carry a FK to journal.code
-- (journal_entry_journal_code_fkey / sequence_piece_journal_code_fkey,
-- V1__baseline.sql), but V1 never seeded journal itself, so every insert
-- into journal_entry (e.g. JournalEntryRepositoryAdapter.nextPieceNumber
-- posting a sale/fund-call entry) failed that FK check on Postgres. Invisible
-- in the dev profile, where ddl-auto: create-drop regenerates schema from
-- JPA entities only - `journal` has no JPA entity of its own (it's read-only
-- reference data, see JournalCode.java), so neither the table nor its FK
-- exists there at all.
INSERT INTO journal (code, label, type, treasury_role, postable) VALUES
    ('VT', 'Ventes / Appels de fonds', 'SALES', NULL, true),
    ('BQ', 'Banque', 'TREASURY', 'BANK', true),
    ('CA', 'Caisse', 'TREASURY', 'CASH', true),
    ('AC', 'Achats', 'PURCHASES', NULL, true),
    ('OD', 'Operations diverses', 'MISCELLANEOUS', NULL, true),
    ('AN', 'A-nouveaux', 'OPENING', NULL, false);
