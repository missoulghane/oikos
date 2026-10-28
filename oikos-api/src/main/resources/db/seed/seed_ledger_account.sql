-- Plan de comptes partagé « usage syndic », sous une forme rejouable.
--
-- Même raison d'être que les autres fichiers de db/seed/ : le profil dev tourne
-- sur H2 avec Flyway désactivé et un schéma généré par Hibernate, il n'applique
-- donc jamais V1__baseline.sql, qui pose ces lignes pour un vrai déploiement.
-- Sans ce fichier, un appel de fonds ou un règlement échouerait en dev faute de
-- compte de cotisations ou d'avances (AccountRoleNotConfiguredException).
--
-- La forme INSERT ... SELECT ... WHERE NOT EXISTS est celle que H2 accepte aussi
-- (pas de ON CONFLICT), et elle rend le script rejouable à volonté.
--
-- LedgerAccountSeedTest vérifie que ce fichier et V1 déclarent les mêmes comptes :
-- la duplication est assumée, sa dérive ne l'est pas.

INSERT INTO ledger_account (id, property_id, unit_id, account_number, label, account_class, nature, collective, role, active, balance, created_date, last_modified_date, version)
SELECT '00000000-0000-0000-0000-000000000001', NULL, NULL, '44150000', 'Coproprietaires - avances et acomptes recus', 4, 'BALANCE_LIABILITY', true, 'UNIT_ADVANCE', true, 0, now(), now(), 0
WHERE NOT EXISTS (SELECT 1 FROM ledger_account WHERE id = '00000000-0000-0000-0000-000000000001');

INSERT INTO ledger_account (id, property_id, unit_id, account_number, label, account_class, nature, collective, role, active, balance, created_date, last_modified_date, version)
SELECT '00000000-0000-0000-0000-000000000002', NULL, NULL, '44110000', 'Fournisseurs', 4, 'BALANCE_LIABILITY', true, 'SUPPLIER', true, 0, now(), now(), 0
WHERE NOT EXISTS (SELECT 1 FROM ledger_account WHERE id = '00000000-0000-0000-0000-000000000002');

INSERT INTO ledger_account (id, property_id, unit_id, account_number, label, account_class, nature, collective, role, active, balance, created_date, last_modified_date, version)
SELECT '00000000-0000-0000-0000-000000000003', NULL, NULL, '44320000', 'Personnel - remunerations dues', 4, 'BALANCE_LIABILITY', false, 'STAFF_PAYABLE', true, 0, now(), now(), 0
WHERE NOT EXISTS (SELECT 1 FROM ledger_account WHERE id = '00000000-0000-0000-0000-000000000003');

INSERT INTO ledger_account (id, property_id, unit_id, account_number, label, account_class, nature, collective, role, active, balance, created_date, last_modified_date, version)
SELECT '00000000-0000-0000-0000-000000000004', NULL, NULL, '61220000', 'Achats de fournitures consommables (entretien)', 6, 'EXPENSE', false, NULL, true, 0, now(), now(), 0
WHERE NOT EXISTS (SELECT 1 FROM ledger_account WHERE id = '00000000-0000-0000-0000-000000000004');

INSERT INTO ledger_account (id, property_id, unit_id, account_number, label, account_class, nature, collective, role, active, balance, created_date, last_modified_date, version)
SELECT '00000000-0000-0000-0000-000000000005', NULL, NULL, '61470000', 'Services bancaires', 6, 'EXPENSE', false, NULL, true, 0, now(), now(), 0
WHERE NOT EXISTS (SELECT 1 FROM ledger_account WHERE id = '00000000-0000-0000-0000-000000000005');

INSERT INTO ledger_account (id, property_id, unit_id, account_number, label, account_class, nature, collective, role, active, balance, created_date, last_modified_date, version)
SELECT '00000000-0000-0000-0000-000000000006', NULL, NULL, '61710000', 'Remunerations du personnel', 6, 'EXPENSE', false, NULL, true, 0, now(), now(), 0
WHERE NOT EXISTS (SELECT 1 FROM ledger_account WHERE id = '00000000-0000-0000-0000-000000000006');

INSERT INTO ledger_account (id, property_id, unit_id, account_number, label, account_class, nature, collective, role, active, balance, created_date, last_modified_date, version)
SELECT '00000000-0000-0000-0000-000000000007', NULL, NULL, '71810000', 'Cotisations des coproprietaires', 7, 'INCOME', false, 'DUES_INCOME', true, 0, now(), now(), 0
WHERE NOT EXISTS (SELECT 1 FROM ledger_account WHERE id = '00000000-0000-0000-0000-000000000007');

INSERT INTO ledger_account (id, property_id, unit_id, account_number, label, account_class, nature, collective, role, active, balance, created_date, last_modified_date, version)
SELECT '00000000-0000-0000-0000-000000000008', NULL, NULL, '61250000', 'Eau et electricite', 6, 'EXPENSE', false, NULL, true, 0, now(), now(), 0
WHERE NOT EXISTS (SELECT 1 FROM ledger_account WHERE id = '00000000-0000-0000-0000-000000000008');

INSERT INTO ledger_account (id, property_id, unit_id, account_number, label, account_class, nature, collective, role, active, balance, created_date, last_modified_date, version)
SELECT '00000000-0000-0000-0000-000000000009', NULL, NULL, '61330000', 'Entretien et reparations', 6, 'EXPENSE', false, NULL, true, 0, now(), now(), 0
WHERE NOT EXISTS (SELECT 1 FROM ledger_account WHERE id = '00000000-0000-0000-0000-000000000009');

INSERT INTO ledger_account (id, property_id, unit_id, account_number, label, account_class, nature, collective, role, active, balance, created_date, last_modified_date, version)
SELECT '00000000-0000-0000-0000-000000000010', NULL, NULL, '61340000', 'Primes d assurance', 6, 'EXPENSE', false, NULL, true, 0, now(), now(), 0
WHERE NOT EXISTS (SELECT 1 FROM ledger_account WHERE id = '00000000-0000-0000-0000-000000000010');

INSERT INTO ledger_account (id, property_id, unit_id, account_number, label, account_class, nature, collective, role, active, balance, created_date, last_modified_date, version)
SELECT '00000000-0000-0000-0000-000000000011', NULL, NULL, '61360000', 'Remuneration du syndic', 6, 'EXPENSE', false, NULL, true, 0, now(), now(), 0
WHERE NOT EXISTS (SELECT 1 FROM ledger_account WHERE id = '00000000-0000-0000-0000-000000000011');

INSERT INTO ledger_account (id, property_id, unit_id, account_number, label, account_class, nature, collective, role, active, balance, created_date, last_modified_date, version)
SELECT '00000000-0000-0000-0000-000000000012', NULL, NULL, '61740000', 'Charges sociales (CNSS/AMO)', 6, 'EXPENSE', false, NULL, true, 0, now(), now(), 0
WHERE NOT EXISTS (SELECT 1 FROM ledger_account WHERE id = '00000000-0000-0000-0000-000000000012');

INSERT INTO ledger_account (id, property_id, unit_id, account_number, label, account_class, nature, collective, role, active, balance, created_date, last_modified_date, version)
SELECT '00000000-0000-0000-0000-000000000013', NULL, NULL, '44540000', 'Etat - charges sociales a payer', 4, 'BALANCE_LIABILITY', false, NULL, true, 0, now(), now(), 0
WHERE NOT EXISTS (SELECT 1 FROM ledger_account WHERE id = '00000000-0000-0000-0000-000000000013');

INSERT INTO ledger_account (id, property_id, unit_id, account_number, label, account_class, nature, collective, role, active, balance, created_date, last_modified_date, version)
SELECT '00000000-0000-0000-0000-000000000014', NULL, NULL, '11100000', 'Fonds de reserve / travaux', 1, 'BALANCE_LIABILITY', false, NULL, true, 0, now(), now(), 0
WHERE NOT EXISTS (SELECT 1 FROM ledger_account WHERE id = '00000000-0000-0000-0000-000000000014');

INSERT INTO ledger_account (id, property_id, unit_id, account_number, label, account_class, nature, collective, role, active, balance, created_date, last_modified_date, version)
SELECT '00000000-0000-0000-0000-000000000015', NULL, NULL, '11900000', 'Resultat de l exercice', 1, 'BALANCE_LIABILITY', false, NULL, true, 0, now(), now(), 0
WHERE NOT EXISTS (SELECT 1 FROM ledger_account WHERE id = '00000000-0000-0000-0000-000000000015');
