-- =========================================================================
-- dev-only: complete dataset covering user/auth, property (properties,
-- buildings, units, ownerships, board) and party together, so the whole
-- application can be explored manually (Swagger UI, H2 console) without
-- going through the use cases first.
-- =========================================================================

-- =========================================================================
-- 0. RBAC: role -> permission bundles. Flyway is disabled for the dev
-- profile (ddl-auto: create-drop, schema regenerated from JPA entities), so
-- this mirrors V2__rbac_permissions.sql's role_permission seed - kept in
-- sync manually, same as the admin account below duplicating V1's seed row.
-- No JPA entity maps the `permission` table itself (nothing in the app
-- reads it directly, only role_permission is queried), so under
-- Hibernate-generated schema (unlike the real V2 Flyway migration) that
-- table - and its FK from role_permission - simply doesn't exist here;
-- role_permission.permission_key is seeded as a plain string below.
-- =========================================================================

INSERT INTO role_permission (role_name, permission_key) VALUES
    ('ROLE_ADMIN', 'property:read'),
    ('ROLE_ADMIN', 'property:create'),
    ('ROLE_ADMIN', 'property:update'),
    ('ROLE_ADMIN', 'property:board:manage'),
    ('ROLE_ADMIN', 'property:member:invite'),
    ('ROLE_ADMIN', 'unit:read'),
    ('ROLE_ADMIN', 'unit:write'),
    ('ROLE_ADMIN', 'unit:ownership:write'),
    ('ROLE_ADMIN', 'party:read'),
    ('ROLE_ADMIN', 'party:write'),
    ('ROLE_ADMIN', 'party:invite'),
    ('ROLE_ADMIN', 'installment:read'),
    ('ROLE_ADMIN', 'installment:call:write'),
    ('ROLE_ADMIN', 'property:accounting:read'),
    ('ROLE_ADMIN', 'property:accounting:write'),
    ('ROLE_ADMIN', 'user:admin');

INSERT INTO role_permission (role_name, permission_key) VALUES
    ('PROPERTY_BOARD_ADMIN', 'property:read'),
    ('PROPERTY_BOARD_ADMIN', 'property:create'),
    ('PROPERTY_BOARD_ADMIN', 'property:update'),
    ('PROPERTY_BOARD_ADMIN', 'property:board:manage'),
    ('PROPERTY_BOARD_ADMIN', 'property:member:invite'),
    ('PROPERTY_BOARD_ADMIN', 'unit:read'),
    ('PROPERTY_BOARD_ADMIN', 'unit:write'),
    ('PROPERTY_BOARD_ADMIN', 'unit:ownership:write'),
    ('PROPERTY_BOARD_ADMIN', 'party:read'),
    ('PROPERTY_BOARD_ADMIN', 'party:write'),
    ('PROPERTY_BOARD_ADMIN', 'party:invite'),
    ('PROPERTY_BOARD_ADMIN', 'installment:read'),
    ('PROPERTY_BOARD_ADMIN', 'installment:call:write'),
    ('PROPERTY_BOARD_ADMIN', 'property:accounting:read'),
    ('PROPERTY_BOARD_ADMIN', 'property:accounting:write');

INSERT INTO role_permission (role_name, permission_key) VALUES
    ('PROPERTY_BOARD_MEMBER', 'property:read'),
    ('PROPERTY_BOARD_MEMBER', 'property:update'),
    ('PROPERTY_BOARD_MEMBER', 'property:board:manage'),
    ('PROPERTY_BOARD_MEMBER', 'unit:read'),
    ('PROPERTY_BOARD_MEMBER', 'unit:write'),
    ('PROPERTY_BOARD_MEMBER', 'unit:ownership:write'),
    ('PROPERTY_BOARD_MEMBER', 'party:read'),
    ('PROPERTY_BOARD_MEMBER', 'party:write'),
    ('PROPERTY_BOARD_MEMBER', 'party:invite'),
    ('PROPERTY_BOARD_MEMBER', 'installment:read'),
    ('PROPERTY_BOARD_MEMBER', 'installment:call:write'),
    ('PROPERTY_BOARD_MEMBER', 'property:accounting:read'),
    ('PROPERTY_BOARD_MEMBER', 'property:accounting:write');

INSERT INTO role_permission (role_name, permission_key)
SELECT 'PROPERTY_MANAGER_ADMIN', permission_key FROM role_permission WHERE role_name = 'PROPERTY_BOARD_ADMIN';

INSERT INTO role_permission (role_name, permission_key)
SELECT 'PROPERTY_MANAGER_MEMBER', permission_key FROM role_permission WHERE role_name = 'PROPERTY_BOARD_MEMBER';

INSERT INTO role_permission (role_name, permission_key) VALUES
    ('PROPERTY_OWNER', 'party:read'),
    ('PROPERTY_OWNER', 'unit:read'),
    ('PROPERTY_OWNER', 'installment:read');

-- =========================================================================
-- 1. PROPERTIES (created before any Party, which now belongs to one)
-- =========================================================================

-- PROPERTY 1: Résidence Test - 2 buildings, 4 units (1 unsold)
INSERT INTO property (id, name, address, created_date, last_modified_date, version) VALUES
    ('11111111-0000-0000-0000-000000000001', 'Résidence Test', '1 rue de la Paix, Casablanca', now(), now(), 0);

INSERT INTO building (id, property_id, name, floor_count, created_date, last_modified_date, version) VALUES
    ('11111111-0000-0000-0000-000000000002', '11111111-0000-0000-0000-000000000001', 'Bâtiment A', 3, now(), now(), 0),
    ('11111111-0000-0000-0000-000000000006', '11111111-0000-0000-0000-000000000001', 'Bâtiment B', 2, now(), now(), 0);

INSERT INTO unit_type_definition (id, property_id, name, created_date, last_modified_date, version) VALUES
    ('11111111-0000-0000-0000-000000000020', '11111111-0000-0000-0000-000000000001', 'Appartement', now(), now(), 0),
    ('11111111-0000-0000-0000-000000000021', '11111111-0000-0000-0000-000000000001', 'Box', now(), now(), 0);

INSERT INTO unit (id, building_id, property_id, unit_number, unit_type_id, shares, created_date, last_modified_date, version) VALUES
    ('11111111-0000-0000-0000-000000000003', '11111111-0000-0000-0000-000000000002', '11111111-0000-0000-0000-000000000001', 'Appartement 1', '11111111-0000-0000-0000-000000000020', 100.00, now(), now(), 0),
    ('11111111-0000-0000-0000-000000000004', '11111111-0000-0000-0000-000000000002', '11111111-0000-0000-0000-000000000001', 'Appartement 2', '11111111-0000-0000-0000-000000000020', 100.00, now(), now(), 0),
    -- Unsold: no unit_ownership row (RG-LOT-01 -> reported as UNSOLD_DEVELOPER at read time).
    ('11111111-0000-0000-0000-000000000005', '11111111-0000-0000-0000-000000000002', '11111111-0000-0000-0000-000000000001', 'Box 1', '11111111-0000-0000-0000-000000000021', 10.00, now(), now(), 0),
    ('11111111-0000-0000-0000-000000000007', '11111111-0000-0000-0000-000000000006', '11111111-0000-0000-0000-000000000001', 'Appartement 1', '11111111-0000-0000-0000-000000000020', 100.00, now(), now(), 0);

-- PROPERTY 2: Résidence Les Oliviers - 1 building, 2 units
INSERT INTO property (id, name, address, created_date, last_modified_date, version) VALUES
    ('22222222-0000-0000-0000-000000000001', 'Résidence Les Oliviers', '12 avenue Hassan II, Rabat', now(), now(), 0);

INSERT INTO building (id, property_id, name, floor_count, created_date, last_modified_date, version) VALUES
    ('22222222-0000-0000-0000-000000000002', '22222222-0000-0000-0000-000000000001', 'Bâtiment Unique', 4, now(), now(), 0);

INSERT INTO unit_type_definition (id, property_id, name, created_date, last_modified_date, version) VALUES
    ('22222222-0000-0000-0000-000000000020', '22222222-0000-0000-0000-000000000001', 'Appartement', now(), now(), 0);

INSERT INTO unit (id, building_id, property_id, unit_number, unit_type_id, shares, created_date, last_modified_date, version) VALUES
    ('22222222-0000-0000-0000-000000000003', '22222222-0000-0000-0000-000000000002', '22222222-0000-0000-0000-000000000001', 'Appartement 1', '22222222-0000-0000-0000-000000000020', 100.00, now(), now(), 0),
    -- Unsold: no unit_ownership row.
    ('22222222-0000-0000-0000-000000000004', '22222222-0000-0000-0000-000000000002', '22222222-0000-0000-0000-000000000001', 'Appartement 2', '22222222-0000-0000-0000-000000000020', 100.00, now(), now(), 0);

-- =========================================================================
-- 2. USERS
-- =========================================================================

-- Admin / master account: admin@oikos.com / Iss0ulgh@ne - platform-wide,
-- not tied to any property, so no Party is created for it.
-- Password hash below is BCrypt("Iss0ulgh@ne"), generated with the same
-- algorithm/strength as com.architek.oikos.shared.infrastructure.configuration.PasswordEncoderConfiguration.
INSERT INTO app_user (id, email, full_name, password_hash, verified, enabled, created_date, last_modified_date, version) VALUES
    ('402888b2-2370-4c5e-aba6-985da776bb17', 'admin@oikos.com', 'Oikos Admin',
     '$2b$10$fD6RhlvoiU6Mf9MKR2ukBeIoXa51FfdrZwRQ2TZc.wWr/yzxmMLbW', TRUE, TRUE, now(), now(), 0);

INSERT INTO user_role (user_id, role) VALUES
    ('402888b2-2370-4c5e-aba6-985da776bb17', 'ROLE_MASTER'),
    ('402888b2-2370-4c5e-aba6-985da776bb17', 'ROLE_ADMIN');

-- Property manager account: manager@oikos.com / Manager@2026 - linked to a
-- Party scoped to Résidence Test, with a property-scoped PROPERTY_MANAGER_ADMIN
-- grant; also seated on that property's board (see section 5).
INSERT INTO party (id, property_id, full_name, party_type, email, phone, created_date, last_modified_date, version) VALUES
    ('90000000-0000-0000-0000-000000000001', '11111111-0000-0000-0000-000000000001', 'Karim Alami', 'INDIVIDUAL', 'manager.party@oikos.com', '0600000000', now(), now(), 0);

-- Password hash below is BCrypt("Manager@2026"), same algorithm/strength as above.
INSERT INTO app_user (id, email, full_name, password_hash, verified, enabled, created_date, last_modified_date, version) VALUES
    ('90000000-0000-0000-0000-000000000001', 'manager@oikos.com', 'Karim Alami',
     '$2b$10$iq6wNFfKe5VVvvHD9qaGYuONnhcgElfUOW7WNlSFccQ7tmIHZqKHC', TRUE, TRUE, now(), now(), 0);

INSERT INTO user_role (user_id, role) VALUES
    ('90000000-0000-0000-0000-000000000001', 'ROLE_USER');

INSERT INTO app_user_party (app_user_id, party_id) VALUES
    ('90000000-0000-0000-0000-000000000001', '90000000-0000-0000-0000-000000000001');

INSERT INTO app_user_party_role (app_user_id, party_id, property_id, role) VALUES
    ('90000000-0000-0000-0000-000000000001', '90000000-0000-0000-0000-000000000001',
     '11111111-0000-0000-0000-000000000001', 'PROPERTY_MANAGER_ADMIN');

-- =========================================================================
-- 3. PARTIES (copropriétaires) + unit ownerships
-- =========================================================================

INSERT INTO party (id, property_id, full_name, party_type, email, phone, created_date, last_modified_date, version) VALUES
    ('33333333-0000-0000-0000-000000000001', '11111111-0000-0000-0000-000000000001', 'Jean Dupont', 'INDIVIDUAL', 'jean.dupont@example.com', NULL, now(), now(), 0),
    ('33333333-0000-0000-0000-000000000002', '11111111-0000-0000-0000-000000000001', 'Marie Martin', 'INDIVIDUAL', 'marie.martin@example.com', NULL, now(), now(), 0),
    ('33333333-0000-0000-0000-000000000003', '11111111-0000-0000-0000-000000000001', 'Ahmed Benali', 'INDIVIDUAL', 'ahmed.benali@example.com', NULL, now(), now(), 0),
    ('33333333-0000-0000-0000-000000000004', '22222222-0000-0000-0000-000000000001', 'Sophie Bernard', 'INDIVIDUAL', 'sophie.bernard@example.com', NULL, now(), now(), 0);

INSERT INTO unit_ownership (id, unit_id, party_id, property_id, ownership_share, created_date, last_modified_date, version) VALUES
    -- Jean Dupont owns Résidence Test / Bâtiment A / Appartement 1
    ('88888888-0000-0000-0000-000000000001', '11111111-0000-0000-0000-000000000003', '33333333-0000-0000-0000-000000000001', '11111111-0000-0000-0000-000000000001', 100.00, now(), now(), 0),
    -- Marie Martin owns Résidence Test / Bâtiment A / Appartement 2
    ('88888888-0000-0000-0000-000000000002', '11111111-0000-0000-0000-000000000004', '33333333-0000-0000-0000-000000000002', '11111111-0000-0000-0000-000000000001', 100.00, now(), now(), 0),
    -- Ahmed Benali owns Résidence Test / Bâtiment B / Appartement 1
    ('88888888-0000-0000-0000-000000000003', '11111111-0000-0000-0000-000000000007', '33333333-0000-0000-0000-000000000003', '11111111-0000-0000-0000-000000000001', 100.00, now(), now(), 0),
    -- Sophie Bernard owns Résidence Les Oliviers / Appartement 1
    ('88888888-0000-0000-0000-000000000004', '22222222-0000-0000-0000-000000000003', '33333333-0000-0000-0000-000000000004', '22222222-0000-0000-0000-000000000001', 100.00, now(), now(), 0);

-- =========================================================================
-- 4. BOARD: the property manager's party sits on Résidence Test's board
-- =========================================================================

INSERT INTO board_member (id, property_id, party_id, board_role, created_date, last_modified_date, version) VALUES
    ('99999999-0000-0000-0000-000000000001', '11111111-0000-0000-0000-000000000001', '90000000-0000-0000-0000-000000000001', 'PROPERTY_MANAGER', now(), now(), 0);

-- =========================================================================
-- 5. INSTALLMENTS: one per lot, illustrating the two possible statuses
-- (NOT_PAID / OVERDUE - never stored, computed from due_date at read time).
-- =========================================================================

-- Jean Dupont's lot: not yet due (NOT_PAID).
INSERT INTO installment (id, unit_id, due_date, amount, created_date, last_modified_date, version) VALUES
    ('55555555-0000-0000-0000-000000000001', '11111111-0000-0000-0000-000000000003', '2030-01-01', 250.00, now(), now(), 0);

-- Marie Martin's lot: not yet due (NOT_PAID).
INSERT INTO installment (id, unit_id, due_date, amount, created_date, last_modified_date, version) VALUES
    ('55555555-0000-0000-0000-000000000003', '11111111-0000-0000-0000-000000000004', '2030-06-01', 300.00, now(), now(), 0);

-- Ahmed Benali's lot: past due (OVERDUE).
INSERT INTO installment (id, unit_id, due_date, amount, created_date, last_modified_date, version) VALUES
    ('55555555-0000-0000-0000-000000000004', '11111111-0000-0000-0000-000000000007', '2024-01-01', 300.00, now(), now(), 0);
