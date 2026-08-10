-- =========================================================================
-- V11: PCM (Plan Comptable Marocain) referential - replaces the old
-- financial_account/unit_account model entirely (see ADR 0001,
-- docs/adr/0001-comptabilite-pcm-cadrage.md). Two tables:
--
-- - journal: a static, global catalog of the 6 journal codes (spec S3.3).
--   Not property-scoped - which specific treasury ledger_account a BQ/CA
--   entry moves is chosen per journal_entry (treasury_account_id column,
--   V13), not pre-bound per property here. This mirrors how the old
--   FinancialJournalEntry.financialAccountId worked (a direct reference per
--   entry), and avoids a redundant per-property "journal instance" row.
--
-- - ledger_account: the chart of accounts. property_id/unit_id are NULL for
--   shared/global accounts (the common "usage syndic" chart seeded below -
--   segregation of funds, S1, is enforced by journal_entry.property_id, not
--   by duplicating these rows per property) and set only for the
--   property/unit-specific instances that legitimately need their own
--   distinct account per ADR 0001 decision 5/6: CASH/BANK (a property can
--   hold several) and the per-unit UNIT_RECEIVABLE account (one dedicated
--   account per lot, not a shared collective + auxiliary, per the
--   "exigence supplementaire").
--
-- - ledger_account_number_sequence: allocates the incremental suffix used
--   when provisioning a property's CASH/BANK accounts or a unit's
--   receivable account (accountNumber = prefix + zero-padded increment),
--   under a row lock, same technique as sequence_piece (V12, I7).
--
-- Account numbers are plain VARCHAR business data, never reused as a
-- technical key (id stays a GUID everywhere, per the "exigence
-- supplementaire").
-- =========================================================================

CREATE TABLE journal (
    code            VARCHAR(4) PRIMARY KEY,
    label           VARCHAR(100) NOT NULL,
    type            VARCHAR(20) NOT NULL,
    treasury_role   VARCHAR(30),
    postable        BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO journal (code, label, type, treasury_role, postable) VALUES
    ('VT', 'Ventes / Appels de fonds', 'SALES', NULL, TRUE),
    ('BQ', 'Banque', 'TREASURY', 'BANK', TRUE),
    ('CA', 'Caisse', 'TREASURY', 'CASH', TRUE),
    ('AC', 'Achats', 'PURCHASES', NULL, TRUE),
    ('OD', 'Operations diverses', 'MISCELLANEOUS', NULL, TRUE),
    ('AN', 'A-nouveaux', 'OPENING', NULL, FALSE);

CREATE TABLE ledger_account (
    id                  UUID PRIMARY KEY,
    property_id         UUID REFERENCES property (id),
    unit_id             UUID REFERENCES unit (id),
    account_number      VARCHAR(8) NOT NULL,
    label               VARCHAR(200) NOT NULL,
    account_class       INTEGER NOT NULL,
    nature              VARCHAR(20) NOT NULL,
    collective          BOOLEAN NOT NULL DEFAULT FALSE,
    role                VARCHAR(30),
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0
);

-- A unit-specific account always belongs to that unit's own property too.
ALTER TABLE ledger_account ADD CONSTRAINT chk_ledger_account_unit_requires_property
    CHECK (unit_id IS NULL OR property_id IS NOT NULL);

CREATE UNIQUE INDEX uk_ledger_account_scoped_number ON ledger_account (property_id, account_number)
    WHERE property_id IS NOT NULL;
CREATE UNIQUE INDEX uk_ledger_account_global_number ON ledger_account (account_number)
    WHERE property_id IS NULL;
CREATE INDEX idx_ledger_account_property_role ON ledger_account (property_id, role);
CREATE INDEX idx_ledger_account_unit_id ON ledger_account (unit_id);

CREATE TABLE ledger_account_number_sequence (
    property_id     UUID NOT NULL REFERENCES property (id),
    number_prefix   VARCHAR(6) NOT NULL,
    next_increment  INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (property_id, number_prefix)
);

-- Seed of the "usage syndic" chart of accounts (spec S3.2), shared/global
-- (property_id/unit_id NULL). Second referential ("PCGE strict") is
-- deliberately not seeded yet - ADR 0001 decision 4. normal_side is not a
-- column - it is always derived from nature (AccountNature.normalSide()),
-- never stored, so nothing here can drift out of sync with it.
INSERT INTO ledger_account (id, property_id, unit_id, account_number, label, account_class, nature, collective, role, active, created_date, last_modified_date, version) VALUES
    ('00000000-0000-0000-0000-000000000001', NULL, NULL, '44150000', 'Coproprietaires - avances et acomptes recus', 4, 'BALANCE_LIABILITY', TRUE, 'UNIT_ADVANCE', TRUE, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000002', NULL, NULL, '44110000', 'Fournisseurs', 4, 'BALANCE_LIABILITY', TRUE, 'SUPPLIER', TRUE, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000003', NULL, NULL, '44320000', 'Personnel - remunerations dues', 4, 'BALANCE_LIABILITY', FALSE, 'STAFF_PAYABLE', TRUE, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000004', NULL, NULL, '61220000', 'Achats de fournitures consommables (entretien)', 6, 'EXPENSE', FALSE, NULL, TRUE, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000005', NULL, NULL, '61470000', 'Services bancaires', 6, 'EXPENSE', FALSE, NULL, TRUE, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000006', NULL, NULL, '61710000', 'Remunerations du personnel', 6, 'EXPENSE', FALSE, NULL, TRUE, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000007', NULL, NULL, '71810000', 'Cotisations des coproprietaires', 7, 'INCOME', FALSE, 'DUES_INCOME', TRUE, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000008', NULL, NULL, '61250000', 'Eau et electricite', 6, 'EXPENSE', FALSE, NULL, TRUE, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000009', NULL, NULL, '61330000', 'Entretien et reparations', 6, 'EXPENSE', FALSE, NULL, TRUE, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000010', NULL, NULL, '61340000', 'Primes d assurance', 6, 'EXPENSE', FALSE, NULL, TRUE, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000011', NULL, NULL, '61360000', 'Remuneration du syndic', 6, 'EXPENSE', FALSE, NULL, TRUE, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000012', NULL, NULL, '61740000', 'Charges sociales (CNSS/AMO)', 6, 'EXPENSE', FALSE, NULL, TRUE, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000013', NULL, NULL, '44540000', 'Etat - charges sociales a payer', 4, 'BALANCE_LIABILITY', FALSE, NULL, TRUE, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000014', NULL, NULL, '11100000', 'Fonds de reserve / travaux', 1, 'BALANCE_LIABILITY', FALSE, NULL, TRUE, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000015', NULL, NULL, '11900000', 'Resultat de l exercice', 1, 'BALANCE_LIABILITY', FALSE, NULL, TRUE, now(), now(), 0);
