-- Persists each account's balance (its own normal side) instead of
-- recomputing it on every read - updated atomically by PostJournalEntryService
-- whenever an entry is posted. Backfilled here from already-POSTED lines,
-- if any existed before this migration.
ALTER TABLE ledger_account ADD COLUMN balance NUMERIC(14, 2) NOT NULL DEFAULT 0;

UPDATE ledger_account a
SET balance = COALESCE((
    SELECT SUM(
        CASE
            WHEN (a.nature IN ('BALANCE_ASSET', 'EXPENSE') AND l.direction = 'DEBIT')
              OR (a.nature IN ('BALANCE_LIABILITY', 'INCOME') AND l.direction = 'CREDIT')
                THEN l.amount
            ELSE -l.amount
        END
    )
    FROM journal_entry_line l
    JOIN journal_entry e ON e.id = l.journal_entry_id
    WHERE l.ledger_account_id = a.id AND e.status = 'POSTED'
), 0);
