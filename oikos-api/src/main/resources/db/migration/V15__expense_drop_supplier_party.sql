-- Partie 2 (ADR 0001 amendment): suppliers are no longer tracked as a Party.
-- A supplier expense is now just a charge account + label, settled directly
-- against a treasury account (see RecordSupplierPaymentService) - dropping
-- this column also drops its FK to party and its index automatically.
ALTER TABLE expense DROP COLUMN supplier_party_id;
