-- =========================================================================
-- V6: dues calculation mode per property. FLAT_RATE (default) keeps the
-- existing behaviour (installment amount = unit type price, UnitTypePricing).
-- SHARES splits projected_budget across units in proportion to their shares
-- (tantiemes). projected_budget is only meaningful/required in SHARES mode -
-- see GenerateInstallmentCallUseCase.
-- =========================================================================

ALTER TABLE property ADD COLUMN dues_calculation_mode VARCHAR(20) NOT NULL DEFAULT 'FLAT_RATE';
ALTER TABLE property ADD COLUMN projected_budget NUMERIC(12,2);
