-- =========================================================================
-- V5: an installment's settlement status (Non soldee/Partiellement
-- soldee/Soldee) is now stored (outstanding_amount) instead of computed
-- from its due date, kept in sync by accounting on every lettrage
-- validation (see accounting.application.port.out.InstallmentSettlementPort).
-- Backfilled conservatively as "fully outstanding" rather than a NOT NULL
-- DEFAULT that would silently mark existing rows as settled.
-- =========================================================================

ALTER TABLE installment ADD COLUMN outstanding_amount NUMERIC(12, 2);
UPDATE installment SET outstanding_amount = amount;
ALTER TABLE installment ALTER COLUMN outstanding_amount SET NOT NULL;
