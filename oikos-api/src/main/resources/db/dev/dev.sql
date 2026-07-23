-- =========================================================================
-- dev-only: complete dataset covering user/auth, property (properties,
-- buildings, units, ownerships, board), party and accounting together, so
-- the whole application can be explored manually (Swagger UI, H2 console)
-- without going through the use cases first.
-- =========================================================================

-- =========================================================================
-- 1. USERS
-- =========================================================================

-- Admin / master account: admin@oikos.com / Iss0ulgh@ne
INSERT INTO party (id, full_name, party_type, email, phone, created_date, last_modified_date, version) VALUES
    ('402888b2-2370-4c5e-aba6-985da776bb17', 'Oikos Admin', 'INDIVIDUAL', 'admin@oikos.com', NULL, now(), now(), 0);

-- Password hash below is BCrypt("Iss0ulgh@ne"), generated with the same
-- algorithm/strength as com.architek.oikos.shared.infrastructure.configuration.PasswordEncoderConfiguration.
INSERT INTO app_user (id, party_id, login, password_hash, verified, enabled, created_date, last_modified_date, version) VALUES
    ('402888b2-2370-4c5e-aba6-985da776bb17', '402888b2-2370-4c5e-aba6-985da776bb17', 'admin@oikos.com',
     '$2b$10$fD6RhlvoiU6Mf9MKR2ukBeIoXa51FfdrZwRQ2TZc.wWr/yzxmMLbW', TRUE, TRUE, now(), now(), 0);

INSERT INTO user_role (user_id, role) VALUES
    ('402888b2-2370-4c5e-aba6-985da776bb17', 'ROLE_MASTER'),
    ('402888b2-2370-4c5e-aba6-985da776bb17', 'ROLE_ADMIN');

-- Property manager account: manager@oikos.com / Manager@2026 - also seated
-- on property 1's board (see section 5).
INSERT INTO party (id, full_name, party_type, email, phone, created_date, last_modified_date, version) VALUES
    ('90000000-0000-0000-0000-000000000001', 'Karim Alami', 'INDIVIDUAL', 'manager@oikos.com', '0600000000', now(), now(), 0);

-- Password hash below is BCrypt("Manager@2026"), same algorithm/strength as above.
INSERT INTO app_user (id, party_id, login, password_hash, verified, enabled, created_date, last_modified_date, version) VALUES
    ('90000000-0000-0000-0000-000000000001', '90000000-0000-0000-0000-000000000001', 'manager@oikos.com',
     '$2b$10$iq6wNFfKe5VVvvHD9qaGYuONnhcgElfUOW7WNlSFccQ7tmIHZqKHC', TRUE, TRUE, now(), now(), 0);

INSERT INTO user_role (user_id, role) VALUES
    ('90000000-0000-0000-0000-000000000001', 'ROLE_PROPERTY_MANAGER');

-- =========================================================================
-- 2. PROPERTY 1: Résidence Test - 2 buildings, 4 units (1 unsold)
-- =========================================================================

INSERT INTO property (id, name, address, created_date, last_modified_date, version) VALUES
    ('11111111-0000-0000-0000-000000000001', 'Résidence Test', '1 rue de la Paix, Casablanca', now(), now(), 0);

INSERT INTO building (id, property_id, name, floor_count, created_date, last_modified_date, version) VALUES
    ('11111111-0000-0000-0000-000000000002', '11111111-0000-0000-0000-000000000001', 'Bâtiment A', 3, now(), now(), 0),
    ('11111111-0000-0000-0000-000000000006', '11111111-0000-0000-0000-000000000001', 'Bâtiment B', 2, now(), now(), 0);

INSERT INTO unit_type_definition (id, property_id, name, created_date, last_modified_date, version) VALUES
    ('11111111-0000-0000-0000-000000000020', '11111111-0000-0000-0000-000000000001', 'Appartement', now(), now(), 0),
    ('11111111-0000-0000-0000-000000000021', '11111111-0000-0000-0000-000000000001', 'Box', now(), now(), 0);

INSERT INTO unit (id, building_id, unit_number, unit_type_id, shares, created_date, last_modified_date, version) VALUES
    ('11111111-0000-0000-0000-000000000003', '11111111-0000-0000-0000-000000000002', 'Appartement 1', '11111111-0000-0000-0000-000000000020', 100.00, now(), now(), 0),
    ('11111111-0000-0000-0000-000000000004', '11111111-0000-0000-0000-000000000002', 'Appartement 2', '11111111-0000-0000-0000-000000000020', 100.00, now(), now(), 0),
    -- Unsold: no unit_ownership row (RG-LOT-01 -> reported as UNSOLD_DEVELOPER at read time).
    ('11111111-0000-0000-0000-000000000005', '11111111-0000-0000-0000-000000000002', 'Box 1', '11111111-0000-0000-0000-000000000021', 10.00, now(), now(), 0),
    ('11111111-0000-0000-0000-000000000007', '11111111-0000-0000-0000-000000000006', 'Appartement 1', '11111111-0000-0000-0000-000000000020', 100.00, now(), now(), 0);

-- =========================================================================
-- 3. PROPERTY 2: Résidence Les Oliviers - 1 building, 2 units
-- =========================================================================

INSERT INTO property (id, name, address, created_date, last_modified_date, version) VALUES
    ('22222222-0000-0000-0000-000000000001', 'Résidence Les Oliviers', '12 avenue Hassan II, Rabat', now(), now(), 0);

INSERT INTO building (id, property_id, name, floor_count, created_date, last_modified_date, version) VALUES
    ('22222222-0000-0000-0000-000000000002', '22222222-0000-0000-0000-000000000001', 'Bâtiment Unique', 4, now(), now(), 0);

INSERT INTO unit_type_definition (id, property_id, name, created_date, last_modified_date, version) VALUES
    ('22222222-0000-0000-0000-000000000020', '22222222-0000-0000-0000-000000000001', 'Appartement', now(), now(), 0);

INSERT INTO unit (id, building_id, unit_number, unit_type_id, shares, created_date, last_modified_date, version) VALUES
    ('22222222-0000-0000-0000-000000000003', '22222222-0000-0000-0000-000000000002', 'Appartement 1', '22222222-0000-0000-0000-000000000020', 100.00, now(), now(), 0),
    -- Unsold: no unit_ownership row.
    ('22222222-0000-0000-0000-000000000004', '22222222-0000-0000-0000-000000000002', 'Appartement 2', '22222222-0000-0000-0000-000000000020', 100.00, now(), now(), 0);

-- =========================================================================
-- 4. PARTIES (copropriétaires) + unit ownerships
-- =========================================================================

INSERT INTO party (id, full_name, party_type, email, phone, created_date, last_modified_date, version) VALUES
    ('33333333-0000-0000-0000-000000000001', 'Jean Dupont', 'INDIVIDUAL', 'jean.dupont@example.com', NULL, now(), now(), 0),
    ('33333333-0000-0000-0000-000000000002', 'Marie Martin', 'INDIVIDUAL', 'marie.martin@example.com', NULL, now(), now(), 0),
    ('33333333-0000-0000-0000-000000000003', 'Ahmed Benali', 'INDIVIDUAL', 'ahmed.benali@example.com', NULL, now(), now(), 0),
    ('33333333-0000-0000-0000-000000000004', 'Sophie Bernard', 'INDIVIDUAL', 'sophie.bernard@example.com', NULL, now(), now(), 0);

INSERT INTO unit_ownership (id, unit_id, party_id, ownership_share, created_date, last_modified_date, version) VALUES
    -- Jean Dupont owns Résidence Test / Bâtiment A / Appartement 1
    ('88888888-0000-0000-0000-000000000001', '11111111-0000-0000-0000-000000000003', '33333333-0000-0000-0000-000000000001', 100.00, now(), now(), 0),
    -- Marie Martin owns Résidence Test / Bâtiment A / Appartement 2
    ('88888888-0000-0000-0000-000000000002', '11111111-0000-0000-0000-000000000004', '33333333-0000-0000-0000-000000000002', 100.00, now(), now(), 0),
    -- Ahmed Benali owns Résidence Test / Bâtiment B / Appartement 1
    ('88888888-0000-0000-0000-000000000003', '11111111-0000-0000-0000-000000000007', '33333333-0000-0000-0000-000000000003', 100.00, now(), now(), 0),
    -- Sophie Bernard owns Résidence Les Oliviers / Appartement 1
    ('88888888-0000-0000-0000-000000000004', '22222222-0000-0000-0000-000000000003', '33333333-0000-0000-0000-000000000004', 100.00, now(), now(), 0);

-- =========================================================================
-- 5. BOARD: the property manager user manages Résidence Test
-- =========================================================================

INSERT INTO board_member (id, property_id, party_id, board_role, created_date, last_modified_date, version) VALUES
    ('99999999-0000-0000-0000-000000000001', '11111111-0000-0000-0000-000000000001', '90000000-0000-0000-0000-000000000001', 'PROPERTY_MANAGER', now(), now(), 0);

-- =========================================================================
-- 6. ACCOUNTING: one account per lot (RG001 revised - not per copropriétaire),
-- each illustrating a different installment status (PAID / PARTIALLY_PAID /
-- NOT_PAID / OVERDUE) plus an advance-payment / credit-balance scenario
-- (spec §4). Plus one mirror account per property (RG010bis): every
-- unit-side movement below is paired with its opposite-direction mirror on
-- that unit's property account, exactly as AccountBalanceService would do at
-- runtime - this seed data is plain SQL, so the mirror has to be written out
-- by hand here to stay consistent with what the app itself would produce.
-- =========================================================================

INSERT INTO account (id, holder_id, account_type, balance, created_date, last_modified_date, version) VALUES
    ('44444444-0000-0000-0000-000000000001', '11111111-0000-0000-0000-000000000003', 'UNIT', -150.00, now(), now(), 0), -- Jean Dupont's lot
    ('44444444-0000-0000-0000-000000000002', '11111111-0000-0000-0000-000000000004', 'UNIT', -300.00, now(), now(), 0), -- Marie Martin's lot
    ('44444444-0000-0000-0000-000000000003', '11111111-0000-0000-0000-000000000007', 'UNIT', -300.00, now(), now(), 0), -- Ahmed Benali's lot
    ('44444444-0000-0000-0000-000000000004', '22222222-0000-0000-0000-000000000003', 'UNIT', 1000.00, now(), now(), 0), -- Sophie Bernard's lot
    ('44444444-0000-0000-0000-000000000005', '11111111-0000-0000-0000-000000000001', 'PROPERTY', 750.00, now(), now(), 0), -- Résidence Test (mirrors Jean+Marie+Ahmed)
    ('44444444-0000-0000-0000-000000000006', '22222222-0000-0000-0000-000000000001', 'PROPERTY', -1000.00, now(), now(), 0); -- Résidence Les Oliviers (mirrors Sophie)

-- --- Jean Dupont's lot: 2 installments - one fully settled (PAID), one
-- partially paid (PARTIALLY_PAID, reste dû 150). Illustrates lettrage on both sides.
INSERT INTO installment (id, account_id, unit_id, due_date, amount, created_date, last_modified_date, version) VALUES
    ('55555555-0000-0000-0000-000000000001', '44444444-0000-0000-0000-000000000001', '11111111-0000-0000-0000-000000000003', '2026-01-01', 250.00, now(), now(), 0),
    ('55555555-0000-0000-0000-000000000002', '44444444-0000-0000-0000-000000000001', '11111111-0000-0000-0000-000000000003', '2030-01-01', 250.00, now(), now(), 0);

INSERT INTO movement (id, account_id, occurred_on, type, direction, amount, label, business_reference, created_date, last_modified_date, version) VALUES
    ('66666666-0000-0000-0000-000000000001', '44444444-0000-0000-0000-000000000001', '2025-12-01T09:00:00Z', 'INSTALLMENT', 'DEBIT', 250.00, 'Appel de cotisation T1', NULL, now(), now(), 0),
    ('66666666-0000-0000-0000-000000000002', '44444444-0000-0000-0000-000000000001', '2025-12-15T09:00:00Z', 'PAYMENT', 'CREDIT', 250.00, 'Paiement par virement', NULL, now(), now(), 0),
    ('66666666-0000-0000-0000-000000000003', '44444444-0000-0000-0000-000000000001', '2029-12-01T09:00:00Z', 'INSTALLMENT', 'DEBIT', 250.00, 'Appel de cotisation T2', NULL, now(), now(), 0),
    ('66666666-0000-0000-0000-000000000004', '44444444-0000-0000-0000-000000000001', '2029-12-10T09:00:00Z', 'PAYMENT', 'CREDIT', 100.00, 'Acompte', NULL, now(), now(), 0);

INSERT INTO allocation (id, movement_id, installment_id, allocated_amount, created_date, last_modified_date, version) VALUES
    ('77777777-0000-0000-0000-000000000001', '66666666-0000-0000-0000-000000000002', '55555555-0000-0000-0000-000000000001', 250.00, now(), now(), 0),
    ('77777777-0000-0000-0000-000000000002', '66666666-0000-0000-0000-000000000004', '55555555-0000-0000-0000-000000000002', 100.00, now(), now(), 0);

-- --- Marie Martin's lot: 1 installment not yet due, no payment (NOT_PAID).
INSERT INTO installment (id, account_id, unit_id, due_date, amount, created_date, last_modified_date, version) VALUES
    ('55555555-0000-0000-0000-000000000003', '44444444-0000-0000-0000-000000000002', '11111111-0000-0000-0000-000000000004', '2030-06-01', 300.00, now(), now(), 0);

INSERT INTO movement (id, account_id, occurred_on, type, direction, amount, label, business_reference, created_date, last_modified_date, version) VALUES
    ('66666666-0000-0000-0000-000000000005', '44444444-0000-0000-0000-000000000002', '2030-05-01T09:00:00Z', 'INSTALLMENT', 'DEBIT', 300.00, 'Appel de cotisation T1', NULL, now(), now(), 0);

-- --- Ahmed Benali's lot: 1 installment past due, no payment (OVERDUE).
INSERT INTO installment (id, account_id, unit_id, due_date, amount, created_date, last_modified_date, version) VALUES
    ('55555555-0000-0000-0000-000000000004', '44444444-0000-0000-0000-000000000003', '11111111-0000-0000-0000-000000000007', '2024-01-01', 300.00, now(), now(), 0);

INSERT INTO movement (id, account_id, occurred_on, type, direction, amount, label, business_reference, created_date, last_modified_date, version) VALUES
    ('66666666-0000-0000-0000-000000000006', '44444444-0000-0000-0000-000000000003', '2023-12-01T09:00:00Z', 'INSTALLMENT', 'DEBIT', 300.00, 'Appel de cotisation T1', NULL, now(), now(), 0);

-- --- Sophie Bernard's lot: advance payment, no installment raised yet
-- (spec §4) - a pure credit balance of +1000, ready to be auto-allocated to
-- whatever installment gets raised next on this account.
INSERT INTO movement (id, account_id, occurred_on, type, direction, amount, label, business_reference, created_date, last_modified_date, version) VALUES
    ('66666666-0000-0000-0000-000000000007', '44444444-0000-0000-0000-000000000004', '2026-01-05T09:00:00Z', 'PAYMENT', 'CREDIT', 1000.00, 'Versement anticipé', NULL, now(), now(), 0);

-- --- RG010bis mirror movements: opposite direction, same amount/type/label,
-- posted on the lot's property account (Jean/Marie/Ahmed -> Résidence Test;
-- Sophie -> Résidence Les Oliviers).
INSERT INTO movement (id, account_id, occurred_on, type, direction, amount, label, business_reference, created_date, last_modified_date, version) VALUES
    ('66666666-0000-0000-0000-000000000008', '44444444-0000-0000-0000-000000000005', '2025-12-01T09:00:00Z', 'INSTALLMENT', 'CREDIT', 250.00, 'Appel de cotisation T1', NULL, now(), now(), 0),
    ('66666666-0000-0000-0000-000000000009', '44444444-0000-0000-0000-000000000005', '2025-12-15T09:00:00Z', 'PAYMENT', 'DEBIT', 250.00, 'Paiement par virement', NULL, now(), now(), 0),
    ('66666666-0000-0000-0000-000000000010', '44444444-0000-0000-0000-000000000005', '2029-12-01T09:00:00Z', 'INSTALLMENT', 'CREDIT', 250.00, 'Appel de cotisation T2', NULL, now(), now(), 0),
    ('66666666-0000-0000-0000-000000000011', '44444444-0000-0000-0000-000000000005', '2029-12-10T09:00:00Z', 'PAYMENT', 'DEBIT', 100.00, 'Acompte', NULL, now(), now(), 0),
    ('66666666-0000-0000-0000-000000000012', '44444444-0000-0000-0000-000000000005', '2030-05-01T09:00:00Z', 'INSTALLMENT', 'CREDIT', 300.00, 'Appel de cotisation T1', NULL, now(), now(), 0),
    ('66666666-0000-0000-0000-000000000013', '44444444-0000-0000-0000-000000000005', '2023-12-01T09:00:00Z', 'INSTALLMENT', 'CREDIT', 300.00, 'Appel de cotisation T1', NULL, now(), now(), 0),
    ('66666666-0000-0000-0000-000000000014', '44444444-0000-0000-0000-000000000006', '2026-01-05T09:00:00Z', 'PAYMENT', 'DEBIT', 1000.00, 'Versement anticipé', NULL, now(), now(), 0);
