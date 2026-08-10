-- =========================================================================
-- dev-only: complete dataset covering user/auth, property (properties,
-- buildings, units, ownerships, board), party and accounting (exercise,
-- financial account, unit account) together, so the whole application can
-- be explored manually (Swagger UI, H2 console) without going through the
-- use cases first.
--
-- Accounts seeded here (every account below shares the same password, Oikos@2026):
--   admin@oikos.com         - system administrator (ROLE_MASTER + ROLE_ADMIN)
--   manager1@oikos.com      - PROPERTY_MANAGER_ADMIN of Copro 1
--   copr1_user1@oikos.com   - PROPERTY_OWNER, Bâtiment 1 / Appartement 1, Copro 1
--   copr1_user2@oikos.com   - PROPERTY_OWNER, Bâtiment 1 / Appartement 2, Copro 1
--   manager2@oikos.com      - PROPERTY_MANAGER_ADMIN of Copro 2.1 and Copro 2.2
--   copr21_user1@oikos.com  - PROPERTY_OWNER, Bâtiment 1 / Appartement 1, Copro 2.1
--   copr21_user2@oikos.com  - PROPERTY_OWNER, Bâtiment 1 / Appartement 2, Copro 2.1
--   copr22_user1@oikos.com  - PROPERTY_OWNER, Bâtiment 1 / Appartement 1, Copro 2.2
--   copr22_user2@oikos.com  - PROPERTY_OWNER, Bâtiment 1 / Appartement 2, Copro 2.2
--   syndic1@oikos.com       - PROPERTY_BOARD_ADMIN of Copro 3 (syndic bénévole)
--   copr3_user1@oikos.com   - PROPERTY_OWNER, Bâtiment 1 / Appartement 1, Copro 3
--   copr3_user2@oikos.com   - PROPERTY_OWNER, Bâtiment 1 / Appartement 2, Copro 3
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
    ('ROLE_ADMIN', 'user:admin'),
    ('ROLE_ADMIN', 'invitation:manage'),
    ('ROLE_ADMIN', 'document:read'),
    ('ROLE_ADMIN', 'document:write'),
    ('ROLE_ADMIN', 'messaging:broadcast');

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
    ('PROPERTY_BOARD_ADMIN', 'property:accounting:write'),
    ('PROPERTY_BOARD_ADMIN', 'invitation:manage'),
    ('PROPERTY_BOARD_ADMIN', 'document:read'),
    ('PROPERTY_BOARD_ADMIN', 'document:write'),
    ('PROPERTY_BOARD_ADMIN', 'messaging:broadcast');

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
    ('PROPERTY_BOARD_MEMBER', 'property:accounting:write'),
    ('PROPERTY_BOARD_MEMBER', 'document:read'),
    ('PROPERTY_BOARD_MEMBER', 'document:write'),
    ('PROPERTY_BOARD_MEMBER', 'messaging:broadcast');

INSERT INTO role_permission (role_name, permission_key)
SELECT 'PROPERTY_MANAGER_ADMIN', permission_key FROM role_permission WHERE role_name = 'PROPERTY_BOARD_ADMIN';

INSERT INTO role_permission (role_name, permission_key)
SELECT 'PROPERTY_MANAGER_MEMBER', permission_key FROM role_permission WHERE role_name = 'PROPERTY_BOARD_MEMBER';

INSERT INTO role_permission (role_name, permission_key) VALUES
    ('PROPERTY_OWNER', 'party:read'),
    ('PROPERTY_OWNER', 'unit:read'),
    ('PROPERTY_OWNER', 'installment:read'),
    ('PROPERTY_OWNER', 'document:read');

-- =========================================================================
-- 1. ADMIN: platform-wide master account, not tied to any property, so no
-- Party is created for it.
-- Password hash below is BCrypt("Oikos@2026"), generated with the same
-- algorithm/strength as com.architek.oikos.shared.infrastructure.configuration.PasswordEncoderConfiguration.
-- =========================================================================

INSERT INTO app_user (id, email, full_name, password_hash, verified, enabled, created_date, last_modified_date, version) VALUES
    ('402888b2-2370-4c5e-aba6-985da776bb17', 'admin@oikos.com', 'Oikos Admin',
     '$2b$10$jpckGCVGgZLfqalSSjZBhume5BA3lUn2WRFJfQwMgQNb2oPtrJoOu', TRUE, TRUE, now(), now(), 0);

INSERT INTO user_role (user_id, role) VALUES
    ('402888b2-2370-4c5e-aba6-985da776bb17', 'ROLE_MASTER'),
    ('402888b2-2370-4c5e-aba6-985da776bb17', 'ROLE_ADMIN');

-- =========================================================================
-- 2. COPRO 1 (property 'a1111111-...-0001'): 1 building, 1 unit type
-- (Appartement, price 200), 2 units. Manager: manager1@oikos.com.
-- Owners: copr1_user1@oikos.com (unit 1), copr1_user2@oikos.com (unit 2).
-- =========================================================================

INSERT INTO property (id, name, address, dues_calculation_mode, created_date, last_modified_date, version) VALUES
    ('a1111111-0000-0000-0000-000000000001', 'Copro 1', '1 rue de la Paix, Casablanca', 'FLAT_RATE', now(), now(), 0);

INSERT INTO building (id, property_id, name, floor_count, created_date, last_modified_date, version) VALUES
    ('a1111111-0000-0000-0000-000000000002', 'a1111111-0000-0000-0000-000000000001', 'Bâtiment 1', 3, now(), now(), 0);

INSERT INTO unit_type_definition (id, property_id, name, created_date, last_modified_date, version) VALUES
    ('a1111111-0000-0000-0000-000000000010', 'a1111111-0000-0000-0000-000000000001', 'Appartement', now(), now(), 0);

INSERT INTO unit_type_pricing (id, property_id, unit_type_id, price, created_date, last_modified_date, version) VALUES
    ('a1111111-0000-0000-0000-000000000011', 'a1111111-0000-0000-0000-000000000001', 'a1111111-0000-0000-0000-000000000010', 200.00, now(), now(), 0);

INSERT INTO unit (id, building_id, property_id, unit_number, unit_type_id, shares, created_date, last_modified_date, version) VALUES
    ('a1111111-0000-0000-0000-000000000003', 'a1111111-0000-0000-0000-000000000002', 'a1111111-0000-0000-0000-000000000001', 'Appartement 1', 'a1111111-0000-0000-0000-000000000010', 100.00, now(), now(), 0),
    ('a1111111-0000-0000-0000-000000000004', 'a1111111-0000-0000-0000-000000000002', 'a1111111-0000-0000-0000-000000000001', 'Appartement 2', 'a1111111-0000-0000-0000-000000000010', 100.00, now(), now(), 0);

-- Manager: party + login account, PROPERTY_MANAGER_ADMIN on Copro 1, seated on its board.
INSERT INTO party (id, property_id, full_name, party_type, email, phone, created_date, last_modified_date, version) VALUES
    ('a1111111-0000-0000-0000-000000000005', 'a1111111-0000-0000-0000-000000000001', 'Manager Copro 1', 'INDIVIDUAL', 'manager1.party@oikos.com', '0600000001', now(), now(), 0);

-- Password hash below is BCrypt("Oikos@2026"), same algorithm/strength as above.
INSERT INTO app_user (id, email, full_name, password_hash, verified, enabled, created_date, last_modified_date, version) VALUES
    ('a1111111-0000-0000-0000-000000000005', 'manager1@oikos.com', 'Manager Copro 1',
     '$2b$10$jpckGCVGgZLfqalSSjZBhume5BA3lUn2WRFJfQwMgQNb2oPtrJoOu', TRUE, TRUE, now(), now(), 0);

INSERT INTO user_role (user_id, role) VALUES
    ('a1111111-0000-0000-0000-000000000005', 'ROLE_USER');

INSERT INTO app_user_party (app_user_id, party_id) VALUES
    ('a1111111-0000-0000-0000-000000000005', 'a1111111-0000-0000-0000-000000000005');

INSERT INTO app_user_party_role (app_user_id, party_id, property_id, role) VALUES
    ('a1111111-0000-0000-0000-000000000005', 'a1111111-0000-0000-0000-000000000005',
     'a1111111-0000-0000-0000-000000000001', 'PROPERTY_MANAGER_ADMIN');

INSERT INTO board_member (id, property_id, party_id, board_role, status, created_date, last_modified_date, version) VALUES
    ('a1111111-0000-0000-0000-000000000006', 'a1111111-0000-0000-0000-000000000001', 'a1111111-0000-0000-0000-000000000005', 'PROPERTY_MANAGER', 'ACTIVE', now(), now(), 0);

-- Owners: party + login account + PROPERTY_OWNER role + unit ownership.
INSERT INTO party (id, property_id, full_name, party_type, email, phone, created_date, last_modified_date, version) VALUES
    ('a1111111-0000-0000-0000-000000000007', 'a1111111-0000-0000-0000-000000000001', 'Copro 1 - User 1', 'INDIVIDUAL', 'copr1_user1.party@oikos.com', NULL, now(), now(), 0),
    ('a1111111-0000-0000-0000-000000000008', 'a1111111-0000-0000-0000-000000000001', 'Copro 1 - User 2', 'INDIVIDUAL', 'copr1_user2.party@oikos.com', NULL, now(), now(), 0);

-- Password hashes below are BCrypt("Oikos@2026").
INSERT INTO app_user (id, email, full_name, password_hash, verified, enabled, created_date, last_modified_date, version) VALUES
    ('a1111111-0000-0000-0000-000000000007', 'copr1_user1@oikos.com', 'Copro 1 - User 1',
     '$2b$10$jpckGCVGgZLfqalSSjZBhume5BA3lUn2WRFJfQwMgQNb2oPtrJoOu', TRUE, TRUE, now(), now(), 0),
    ('a1111111-0000-0000-0000-000000000008', 'copr1_user2@oikos.com', 'Copro 1 - User 2',
     '$2b$10$jpckGCVGgZLfqalSSjZBhume5BA3lUn2WRFJfQwMgQNb2oPtrJoOu', TRUE, TRUE, now(), now(), 0);

INSERT INTO user_role (user_id, role) VALUES
    ('a1111111-0000-0000-0000-000000000007', 'ROLE_USER'),
    ('a1111111-0000-0000-0000-000000000008', 'ROLE_USER');

INSERT INTO app_user_party (app_user_id, party_id) VALUES
    ('a1111111-0000-0000-0000-000000000007', 'a1111111-0000-0000-0000-000000000007'),
    ('a1111111-0000-0000-0000-000000000008', 'a1111111-0000-0000-0000-000000000008');

INSERT INTO app_user_party_role (app_user_id, party_id, property_id, role) VALUES
    ('a1111111-0000-0000-0000-000000000007', 'a1111111-0000-0000-0000-000000000007', 'a1111111-0000-0000-0000-000000000001', 'PROPERTY_OWNER'),
    ('a1111111-0000-0000-0000-000000000008', 'a1111111-0000-0000-0000-000000000008', 'a1111111-0000-0000-0000-000000000001', 'PROPERTY_OWNER');

INSERT INTO unit_ownership (id, unit_id, party_id, property_id, ownership_share, created_date, last_modified_date, version) VALUES
    ('a1111111-0000-0000-0000-000000000012', 'a1111111-0000-0000-0000-000000000003', 'a1111111-0000-0000-0000-000000000007', 'a1111111-0000-0000-0000-000000000001', 100.00, now(), now(), 0),
    ('a1111111-0000-0000-0000-000000000013', 'a1111111-0000-0000-0000-000000000004', 'a1111111-0000-0000-0000-000000000008', 'a1111111-0000-0000-0000-000000000001', 100.00, now(), now(), 0);

-- =========================================================================
-- 3. COPRO 2.1 (property 'a2222221-...-0001'): same configuration as
-- Copro 1. Manager: manager2@oikos.com (also manages Copro 2.2, section 4).
-- Owners: copr21_user1@oikos.com (unit 1), copr21_user2@oikos.com (unit 2).
-- =========================================================================

INSERT INTO property (id, name, address, dues_calculation_mode, created_date, last_modified_date, version) VALUES
    ('a2222221-0000-0000-0000-000000000001', 'Copro 2.1', '2 avenue Hassan II, Rabat', 'FLAT_RATE', now(), now(), 0);

INSERT INTO building (id, property_id, name, floor_count, created_date, last_modified_date, version) VALUES
    ('a2222221-0000-0000-0000-000000000002', 'a2222221-0000-0000-0000-000000000001', 'Bâtiment 1', 3, now(), now(), 0);

INSERT INTO unit_type_definition (id, property_id, name, created_date, last_modified_date, version) VALUES
    ('a2222221-0000-0000-0000-000000000010', 'a2222221-0000-0000-0000-000000000001', 'Appartement', now(), now(), 0);

INSERT INTO unit_type_pricing (id, property_id, unit_type_id, price, created_date, last_modified_date, version) VALUES
    ('a2222221-0000-0000-0000-000000000011', 'a2222221-0000-0000-0000-000000000001', 'a2222221-0000-0000-0000-000000000010', 200.00, now(), now(), 0);

INSERT INTO unit (id, building_id, property_id, unit_number, unit_type_id, shares, created_date, last_modified_date, version) VALUES
    ('a2222221-0000-0000-0000-000000000003', 'a2222221-0000-0000-0000-000000000002', 'a2222221-0000-0000-0000-000000000001', 'Appartement 1', 'a2222221-0000-0000-0000-000000000010', 100.00, now(), now(), 0),
    ('a2222221-0000-0000-0000-000000000004', 'a2222221-0000-0000-0000-000000000002', 'a2222221-0000-0000-0000-000000000001', 'Appartement 2', 'a2222221-0000-0000-0000-000000000010', 100.00, now(), now(), 0);

INSERT INTO party (id, property_id, full_name, party_type, email, phone, created_date, last_modified_date, version) VALUES
    ('a2222221-0000-0000-0000-000000000007', 'a2222221-0000-0000-0000-000000000001', 'Copro 2.1 - User 1', 'INDIVIDUAL', 'copr21_user1.party@oikos.com', NULL, now(), now(), 0),
    ('a2222221-0000-0000-0000-000000000008', 'a2222221-0000-0000-0000-000000000001', 'Copro 2.1 - User 2', 'INDIVIDUAL', 'copr21_user2.party@oikos.com', NULL, now(), now(), 0);

-- Password hashes below are BCrypt("Oikos@2026").
INSERT INTO app_user (id, email, full_name, password_hash, verified, enabled, created_date, last_modified_date, version) VALUES
    ('a2222221-0000-0000-0000-000000000007', 'copr21_user1@oikos.com', 'Copro 2.1 - User 1',
     '$2b$10$jpckGCVGgZLfqalSSjZBhume5BA3lUn2WRFJfQwMgQNb2oPtrJoOu', TRUE, TRUE, now(), now(), 0),
    ('a2222221-0000-0000-0000-000000000008', 'copr21_user2@oikos.com', 'Copro 2.1 - User 2',
     '$2b$10$jpckGCVGgZLfqalSSjZBhume5BA3lUn2WRFJfQwMgQNb2oPtrJoOu', TRUE, TRUE, now(), now(), 0);

INSERT INTO user_role (user_id, role) VALUES
    ('a2222221-0000-0000-0000-000000000007', 'ROLE_USER'),
    ('a2222221-0000-0000-0000-000000000008', 'ROLE_USER');

INSERT INTO app_user_party (app_user_id, party_id) VALUES
    ('a2222221-0000-0000-0000-000000000007', 'a2222221-0000-0000-0000-000000000007'),
    ('a2222221-0000-0000-0000-000000000008', 'a2222221-0000-0000-0000-000000000008');

INSERT INTO app_user_party_role (app_user_id, party_id, property_id, role) VALUES
    ('a2222221-0000-0000-0000-000000000007', 'a2222221-0000-0000-0000-000000000007', 'a2222221-0000-0000-0000-000000000001', 'PROPERTY_OWNER'),
    ('a2222221-0000-0000-0000-000000000008', 'a2222221-0000-0000-0000-000000000008', 'a2222221-0000-0000-0000-000000000001', 'PROPERTY_OWNER');

INSERT INTO unit_ownership (id, unit_id, party_id, property_id, ownership_share, created_date, last_modified_date, version) VALUES
    ('a2222221-0000-0000-0000-000000000012', 'a2222221-0000-0000-0000-000000000003', 'a2222221-0000-0000-0000-000000000007', 'a2222221-0000-0000-0000-000000000001', 100.00, now(), now(), 0),
    ('a2222221-0000-0000-0000-000000000013', 'a2222221-0000-0000-0000-000000000004', 'a2222221-0000-0000-0000-000000000008', 'a2222221-0000-0000-0000-000000000001', 100.00, now(), now(), 0);

-- =========================================================================
-- 4. COPRO 2.2 (property 'a2222222-...-0001'): same configuration as
-- Copro 1. Manager: manager2@oikos.com (also manages Copro 2.1, section 3).
-- Owners: copr22_user1@oikos.com (unit 1), copr22_user2@oikos.com (unit 2).
-- =========================================================================

INSERT INTO property (id, name, address, dues_calculation_mode, created_date, last_modified_date, version) VALUES
    ('a2222222-0000-0000-0000-000000000001', 'Copro 2.2', '3 avenue Hassan II, Rabat', 'FLAT_RATE', now(), now(), 0);

INSERT INTO building (id, property_id, name, floor_count, created_date, last_modified_date, version) VALUES
    ('a2222222-0000-0000-0000-000000000002', 'a2222222-0000-0000-0000-000000000001', 'Bâtiment 1', 3, now(), now(), 0);

INSERT INTO unit_type_definition (id, property_id, name, created_date, last_modified_date, version) VALUES
    ('a2222222-0000-0000-0000-000000000010', 'a2222222-0000-0000-0000-000000000001', 'Appartement', now(), now(), 0);

INSERT INTO unit_type_pricing (id, property_id, unit_type_id, price, created_date, last_modified_date, version) VALUES
    ('a2222222-0000-0000-0000-000000000011', 'a2222222-0000-0000-0000-000000000001', 'a2222222-0000-0000-0000-000000000010', 200.00, now(), now(), 0);

INSERT INTO unit (id, building_id, property_id, unit_number, unit_type_id, shares, created_date, last_modified_date, version) VALUES
    ('a2222222-0000-0000-0000-000000000003', 'a2222222-0000-0000-0000-000000000002', 'a2222222-0000-0000-0000-000000000001', 'Appartement 1', 'a2222222-0000-0000-0000-000000000010', 100.00, now(), now(), 0),
    ('a2222222-0000-0000-0000-000000000004', 'a2222222-0000-0000-0000-000000000002', 'a2222222-0000-0000-0000-000000000001', 'Appartement 2', 'a2222222-0000-0000-0000-000000000010', 100.00, now(), now(), 0);

INSERT INTO party (id, property_id, full_name, party_type, email, phone, created_date, last_modified_date, version) VALUES
    ('a2222222-0000-0000-0000-000000000007', 'a2222222-0000-0000-0000-000000000001', 'Copro 2.2 - User 1', 'INDIVIDUAL', 'copr22_user1.party@oikos.com', NULL, now(), now(), 0),
    ('a2222222-0000-0000-0000-000000000008', 'a2222222-0000-0000-0000-000000000001', 'Copro 2.2 - User 2', 'INDIVIDUAL', 'copr22_user2.party@oikos.com', NULL, now(), now(), 0);

-- Password hashes below are BCrypt("Oikos@2026").
INSERT INTO app_user (id, email, full_name, password_hash, verified, enabled, created_date, last_modified_date, version) VALUES
    ('a2222222-0000-0000-0000-000000000007', 'copr22_user1@oikos.com', 'Copro 2.2 - User 1',
     '$2b$10$jpckGCVGgZLfqalSSjZBhume5BA3lUn2WRFJfQwMgQNb2oPtrJoOu', TRUE, TRUE, now(), now(), 0),
    ('a2222222-0000-0000-0000-000000000008', 'copr22_user2@oikos.com', 'Copro 2.2 - User 2',
     '$2b$10$jpckGCVGgZLfqalSSjZBhume5BA3lUn2WRFJfQwMgQNb2oPtrJoOu', TRUE, TRUE, now(), now(), 0);

INSERT INTO user_role (user_id, role) VALUES
    ('a2222222-0000-0000-0000-000000000007', 'ROLE_USER'),
    ('a2222222-0000-0000-0000-000000000008', 'ROLE_USER');

INSERT INTO app_user_party (app_user_id, party_id) VALUES
    ('a2222222-0000-0000-0000-000000000007', 'a2222222-0000-0000-0000-000000000007'),
    ('a2222222-0000-0000-0000-000000000008', 'a2222222-0000-0000-0000-000000000008');

INSERT INTO app_user_party_role (app_user_id, party_id, property_id, role) VALUES
    ('a2222222-0000-0000-0000-000000000007', 'a2222222-0000-0000-0000-000000000007', 'a2222222-0000-0000-0000-000000000001', 'PROPERTY_OWNER'),
    ('a2222222-0000-0000-0000-000000000008', 'a2222222-0000-0000-0000-000000000008', 'a2222222-0000-0000-0000-000000000001', 'PROPERTY_OWNER');

INSERT INTO unit_ownership (id, unit_id, party_id, property_id, ownership_share, created_date, last_modified_date, version) VALUES
    ('a2222222-0000-0000-0000-000000000012', 'a2222222-0000-0000-0000-000000000003', 'a2222222-0000-0000-0000-000000000007', 'a2222222-0000-0000-0000-000000000001', 100.00, now(), now(), 0),
    ('a2222222-0000-0000-0000-000000000013', 'a2222222-0000-0000-0000-000000000004', 'a2222222-0000-0000-0000-000000000008', 'a2222222-0000-0000-0000-000000000001', 100.00, now(), now(), 0);

-- =========================================================================
-- 5. MANAGER 2: single login account managing both Copro 2.1 and Copro 2.2.
-- A party belongs to exactly one property, so manager2 gets one party per
-- managed copro, both linked to the same app_user via app_user_party.
-- =========================================================================

INSERT INTO party (id, property_id, full_name, party_type, email, phone, created_date, last_modified_date, version) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'a2222221-0000-0000-0000-000000000001', 'Manager Copro 2', 'INDIVIDUAL', 'manager2.copro21.party@oikos.com', '0600000002', now(), now(), 0),
    ('b0000000-0000-0000-0000-000000000002', 'a2222222-0000-0000-0000-000000000001', 'Manager Copro 2', 'INDIVIDUAL', 'manager2.copro22.party@oikos.com', '0600000002', now(), now(), 0);

-- Password hash below is BCrypt("Oikos@2026"), same algorithm/strength as above.
INSERT INTO app_user (id, email, full_name, password_hash, verified, enabled, created_date, last_modified_date, version) VALUES
    ('b0000000-0000-0000-0000-000000000000', 'manager2@oikos.com', 'Manager Copro 2',
     '$2b$10$jpckGCVGgZLfqalSSjZBhume5BA3lUn2WRFJfQwMgQNb2oPtrJoOu', TRUE, TRUE, now(), now(), 0);

INSERT INTO user_role (user_id, role) VALUES
    ('b0000000-0000-0000-0000-000000000000', 'ROLE_USER');

INSERT INTO app_user_party (app_user_id, party_id) VALUES
    ('b0000000-0000-0000-0000-000000000000', 'b0000000-0000-0000-0000-000000000001'),
    ('b0000000-0000-0000-0000-000000000000', 'b0000000-0000-0000-0000-000000000002');

INSERT INTO app_user_party_role (app_user_id, party_id, property_id, role) VALUES
    ('b0000000-0000-0000-0000-000000000000', 'b0000000-0000-0000-0000-000000000001', 'a2222221-0000-0000-0000-000000000001', 'PROPERTY_MANAGER_ADMIN'),
    ('b0000000-0000-0000-0000-000000000000', 'b0000000-0000-0000-0000-000000000002', 'a2222222-0000-0000-0000-000000000001', 'PROPERTY_MANAGER_ADMIN');

INSERT INTO board_member (id, property_id, party_id, board_role, status, created_date, last_modified_date, version) VALUES
    ('b0000000-0000-0000-0000-000000000011', 'a2222221-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 'PROPERTY_MANAGER', 'ACTIVE', now(), now(), 0),
    ('b0000000-0000-0000-0000-000000000012', 'a2222222-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000002', 'PROPERTY_MANAGER', 'ACTIVE', now(), now(), 0);

-- =========================================================================
-- 6. COPRO 3 (property 'a3333333-...-0001'): same configuration as Copro 1,
-- except dues_calculation_mode = SHARES with a 400.00 projected_budget - a
-- ready-made fixture for the tantieme-based calculation (each unit has 100
-- shares out of 200 total, so 200.00 per unit, same as Copro 1's flat price).
-- Manager: syndic1@oikos.com, a volunteer syndic (PROPERTY_BOARD_ADMIN),
-- unlike manager1/manager2 who are professional firms (PROPERTY_MANAGER_ADMIN).
-- Owners: copr3_user1@oikos.com (unit 1), copr3_user2@oikos.com (unit 2).
-- =========================================================================

INSERT INTO property (id, name, address, dues_calculation_mode, projected_budget, created_date, last_modified_date, version) VALUES
    ('a3333333-0000-0000-0000-000000000001', 'Copro 3', '4 avenue Hassan II, Rabat', 'SHARES', 400.00, now(), now(), 0);

INSERT INTO building (id, property_id, name, floor_count, created_date, last_modified_date, version) VALUES
    ('a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Bâtiment 1', 3, now(), now(), 0);

INSERT INTO unit_type_definition (id, property_id, name, created_date, last_modified_date, version) VALUES
    ('a3333333-0000-0000-0000-000000000010', 'a3333333-0000-0000-0000-000000000001', 'Appartement', now(), now(), 0);

INSERT INTO unit_type_pricing (id, property_id, unit_type_id, price, created_date, last_modified_date, version) VALUES
    ('a3333333-0000-0000-0000-000000000011', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000010', 200.00, now(), now(), 0);

INSERT INTO unit (id, building_id, property_id, unit_number, unit_type_id, shares, created_date, last_modified_date, version) VALUES
    ('a3333333-0000-0000-0000-000000000003', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 1', 'a3333333-0000-0000-0000-000000000010', 100.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000004', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 2', 'a3333333-0000-0000-0000-000000000010', 100.00, now(), now(), 0),
    -- 30 additional units (Appartement 3..32) to exercise pagination/scale in the Lots UI.
    ('a3333333-0000-0000-0000-000000000100', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 3', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000101', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 4', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000102', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 5', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000103', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 6', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000104', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 7', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000105', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 8', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000106', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 9', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000107', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 10', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000108', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 11', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000109', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 12', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000110', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 13', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000111', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 14', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000112', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 15', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000113', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 16', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000114', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 17', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000115', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 18', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000116', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 19', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000117', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 20', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000118', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 21', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000119', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 22', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000120', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 23', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000121', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 24', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000122', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 25', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000123', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 26', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000124', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 27', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000125', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 28', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000126', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 29', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000127', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 30', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000128', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 31', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000129', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 32', 'a3333333-0000-0000-0000-000000000010', 50.00, now(), now(), 0);

-- Volunteer syndic: party + login account, PROPERTY_BOARD_ADMIN on Copro 3, seated on its board.
INSERT INTO party (id, property_id, full_name, party_type, email, phone, created_date, last_modified_date, version) VALUES
    ('a3333333-0000-0000-0000-000000000005', 'a3333333-0000-0000-0000-000000000001', 'Syndic Copro 3', 'INDIVIDUAL', 'syndic1.party@oikos.com', '0600000003', now(), now(), 0);

-- Password hash below is BCrypt("Oikos@2026"), same algorithm/strength as above.
INSERT INTO app_user (id, email, full_name, password_hash, verified, enabled, created_date, last_modified_date, version) VALUES
    ('a3333333-0000-0000-0000-000000000005', 'syndic1@oikos.com', 'Syndic Copro 3',
     '$2b$10$jpckGCVGgZLfqalSSjZBhume5BA3lUn2WRFJfQwMgQNb2oPtrJoOu', TRUE, TRUE, now(), now(), 0);

INSERT INTO user_role (user_id, role) VALUES
    ('a3333333-0000-0000-0000-000000000005', 'ROLE_USER');

INSERT INTO app_user_party (app_user_id, party_id) VALUES
    ('a3333333-0000-0000-0000-000000000005', 'a3333333-0000-0000-0000-000000000005');

INSERT INTO app_user_party_role (app_user_id, party_id, property_id, role) VALUES
    ('a3333333-0000-0000-0000-000000000005', 'a3333333-0000-0000-0000-000000000005',
     'a3333333-0000-0000-0000-000000000001', 'PROPERTY_BOARD_ADMIN');

INSERT INTO board_member (id, property_id, party_id, board_role, status, created_date, last_modified_date, version) VALUES
    ('a3333333-0000-0000-0000-000000000006', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000005', 'PROPERTY_MANAGER', 'ACTIVE', now(), now(), 0);

-- Owners: party + login account + PROPERTY_OWNER role + unit ownership.
INSERT INTO party (id, property_id, full_name, party_type, email, phone, created_date, last_modified_date, version) VALUES
    ('a3333333-0000-0000-0000-000000000007', 'a3333333-0000-0000-0000-000000000001', 'Copro 3 - User 1', 'INDIVIDUAL', 'copr3_user1.party@oikos.com', NULL, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000008', 'a3333333-0000-0000-0000-000000000001', 'Copro 3 - User 2', 'INDIVIDUAL', 'copr3_user2.party@oikos.com', NULL, now(), now(), 0);

-- Password hashes below are BCrypt("Oikos@2026").
INSERT INTO app_user (id, email, full_name, password_hash, verified, enabled, created_date, last_modified_date, version) VALUES
    ('a3333333-0000-0000-0000-000000000007', 'copr3_user1@oikos.com', 'Copro 3 - User 1',
     '$2b$10$jpckGCVGgZLfqalSSjZBhume5BA3lUn2WRFJfQwMgQNb2oPtrJoOu', TRUE, TRUE, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000008', 'copr3_user2@oikos.com', 'Copro 3 - User 2',
     '$2b$10$jpckGCVGgZLfqalSSjZBhume5BA3lUn2WRFJfQwMgQNb2oPtrJoOu', TRUE, TRUE, now(), now(), 0);

INSERT INTO user_role (user_id, role) VALUES
    ('a3333333-0000-0000-0000-000000000007', 'ROLE_USER'),
    ('a3333333-0000-0000-0000-000000000008', 'ROLE_USER');

INSERT INTO app_user_party (app_user_id, party_id) VALUES
    ('a3333333-0000-0000-0000-000000000007', 'a3333333-0000-0000-0000-000000000007'),
    ('a3333333-0000-0000-0000-000000000008', 'a3333333-0000-0000-0000-000000000008');

INSERT INTO app_user_party_role (app_user_id, party_id, property_id, role) VALUES
    ('a3333333-0000-0000-0000-000000000007', 'a3333333-0000-0000-0000-000000000007', 'a3333333-0000-0000-0000-000000000001', 'PROPERTY_OWNER'),
    ('a3333333-0000-0000-0000-000000000008', 'a3333333-0000-0000-0000-000000000008', 'a3333333-0000-0000-0000-000000000001', 'PROPERTY_OWNER');

INSERT INTO unit_ownership (id, unit_id, party_id, property_id, ownership_share, created_date, last_modified_date, version) VALUES
    ('a3333333-0000-0000-0000-000000000012', 'a3333333-0000-0000-0000-000000000003', 'a3333333-0000-0000-0000-000000000007', 'a3333333-0000-0000-0000-000000000001', 100.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000013', 'a3333333-0000-0000-0000-000000000004', 'a3333333-0000-0000-0000-000000000008', 'a3333333-0000-0000-0000-000000000001', 100.00, now(), now(), 0);

-- =========================================================================
-- 7. ACCOUNTING (PCM engine, ADR 0001): the shared "usage syndic" chart of
-- accounts (property_id/unit_id NULL, same rows as the V11 Flyway seed -
-- dev boots via Hibernate ddl-auto=create-drop from the JPA entities, which
-- ignores Flyway entirely, hence the duplication here) plus one open
-- exercise per copro. Property/unit-specific accounts (caisse, creance par
-- lot - normally provisioned automatically by ProvisionPropertyCashAccountService/
-- ProvisionUnitReceivableAccountService when a real Property/Unit is created
-- through the API) and the monthly OPEN periods a real OpenAccountingExerciseService
-- call would generate are seeded explicitly below instead, since these
-- properties/units/exercises are inserted as raw SQL above, bypassing those
-- flows. ledger_account_number_sequence is seeded to match, so a real
-- caisse/lot/bank account added later through the app continues the
-- numbering without colliding with the ids seeded here.
-- =========================================================================

INSERT INTO ledger_account (id, property_id, unit_id, account_number, label, account_class, nature, collective, role, active, balance, created_date, last_modified_date, version) VALUES
    ('00000000-0000-0000-0000-000000000001', NULL, NULL, '44150000', 'Coproprietaires - avances et acomptes recus', 4, 'BALANCE_LIABILITY', TRUE, 'UNIT_ADVANCE', TRUE, 0, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000002', NULL, NULL, '44110000', 'Fournisseurs', 4, 'BALANCE_LIABILITY', TRUE, 'SUPPLIER', TRUE, 0, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000003', NULL, NULL, '44320000', 'Personnel - remunerations dues', 4, 'BALANCE_LIABILITY', FALSE, 'STAFF_PAYABLE', TRUE, 0, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000004', NULL, NULL, '61220000', 'Achats de fournitures consommables (entretien)', 6, 'EXPENSE', FALSE, NULL, TRUE, 0, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000005', NULL, NULL, '61470000', 'Services bancaires', 6, 'EXPENSE', FALSE, NULL, TRUE, 0, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000006', NULL, NULL, '61710000', 'Remunerations du personnel', 6, 'EXPENSE', FALSE, NULL, TRUE, 0, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000007', NULL, NULL, '71810000', 'Cotisations des coproprietaires', 7, 'INCOME', FALSE, 'DUES_INCOME', TRUE, 0, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000008', NULL, NULL, '61250000', 'Eau et electricite', 6, 'EXPENSE', FALSE, NULL, TRUE, 0, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000009', NULL, NULL, '61330000', 'Entretien et reparations', 6, 'EXPENSE', FALSE, NULL, TRUE, 0, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000010', NULL, NULL, '61340000', 'Primes d assurance', 6, 'EXPENSE', FALSE, NULL, TRUE, 0, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000011', NULL, NULL, '61360000', 'Remuneration du syndic', 6, 'EXPENSE', FALSE, NULL, TRUE, 0, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000012', NULL, NULL, '61740000', 'Charges sociales (CNSS/AMO)', 6, 'EXPENSE', FALSE, NULL, TRUE, 0, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000013', NULL, NULL, '44540000', 'Etat - charges sociales a payer', 4, 'BALANCE_LIABILITY', FALSE, NULL, TRUE, 0, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000014', NULL, NULL, '11100000', 'Fonds de reserve / travaux', 1, 'BALANCE_LIABILITY', FALSE, NULL, TRUE, 0, now(), now(), 0),
    ('00000000-0000-0000-0000-000000000015', NULL, NULL, '11900000', 'Resultat de l exercice', 1, 'BALANCE_LIABILITY', FALSE, NULL, TRUE, 0, now(), now(), 0);

-- Property-specific accounts (caisse) and unit-specific accounts (creance par lot),
-- matching what ProvisionPropertyCashAccountService/ProvisionUnitReceivableAccountService
-- would provision for these properties/units in real usage.
INSERT INTO ledger_account (id, property_id, unit_id, account_number, label, account_class, nature, collective, role, active, balance, created_date, last_modified_date, version) VALUES
    ('a1111111-0000-0000-0000-000000000030', 'a1111111-0000-0000-0000-000000000001', NULL, '51610001', 'Caisse', 5, 'BALANCE_ASSET', FALSE, 'CASH', TRUE, 0, now(), now(), 0),
    ('a1111111-0000-0000-0000-000000000031', 'a1111111-0000-0000-0000-000000000001', 'a1111111-0000-0000-0000-000000000003', '34115001', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a1111111-0000-0000-0000-000000000032', 'a1111111-0000-0000-0000-000000000001', 'a1111111-0000-0000-0000-000000000004', '34115002', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a2222221-0000-0000-0000-000000000030', 'a2222221-0000-0000-0000-000000000001', NULL, '51610001', 'Caisse', 5, 'BALANCE_ASSET', FALSE, 'CASH', TRUE, 0, now(), now(), 0),
    ('a2222221-0000-0000-0000-000000000031', 'a2222221-0000-0000-0000-000000000001', 'a2222221-0000-0000-0000-000000000003', '34115001', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a2222221-0000-0000-0000-000000000032', 'a2222221-0000-0000-0000-000000000001', 'a2222221-0000-0000-0000-000000000004', '34115002', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a2222222-0000-0000-0000-000000000030', 'a2222222-0000-0000-0000-000000000001', NULL, '51610001', 'Caisse', 5, 'BALANCE_ASSET', FALSE, 'CASH', TRUE, 0, now(), now(), 0),
    ('a2222222-0000-0000-0000-000000000031', 'a2222222-0000-0000-0000-000000000001', 'a2222222-0000-0000-0000-000000000003', '34115001', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a2222222-0000-0000-0000-000000000032', 'a2222222-0000-0000-0000-000000000001', 'a2222222-0000-0000-0000-000000000004', '34115002', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000200', 'a3333333-0000-0000-0000-000000000001', NULL, '51610001', 'Caisse', 5, 'BALANCE_ASSET', FALSE, 'CASH', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000201', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000003', '34115001', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000202', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000004', '34115002', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000203', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000100', '34115003', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000204', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000101', '34115004', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000205', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000102', '34115005', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000206', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000103', '34115006', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000207', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000104', '34115007', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000208', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000105', '34115008', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000209', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000106', '34115009', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-00000000020a', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000107', '34115010', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-00000000020b', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000108', '34115011', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-00000000020c', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000109', '34115012', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-00000000020d', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000110', '34115013', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-00000000020e', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000111', '34115014', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-00000000020f', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000112', '34115015', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000210', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000113', '34115016', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000211', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000114', '34115017', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000212', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000115', '34115018', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000213', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000116', '34115019', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000214', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000117', '34115020', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000215', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000118', '34115021', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000216', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000119', '34115022', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000217', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000120', '34115023', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000218', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000121', '34115024', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000219', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000122', '34115025', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-00000000021a', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000123', '34115026', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-00000000021b', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000124', '34115027', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-00000000021c', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000125', '34115028', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-00000000021d', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000126', '34115029', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-00000000021e', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000127', '34115030', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-00000000021f', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000128', '34115031', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000220', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000129', '34115032', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0);

INSERT INTO ledger_account_number_sequence (property_id, number_prefix, next_increment) VALUES
    ('a1111111-0000-0000-0000-000000000001', '516100', 2),
    ('a1111111-0000-0000-0000-000000000001', '341150', 3),
    ('a2222221-0000-0000-0000-000000000001', '516100', 2),
    ('a2222221-0000-0000-0000-000000000001', '341150', 3),
    ('a2222222-0000-0000-0000-000000000001', '516100', 2),
    ('a2222222-0000-0000-0000-000000000001', '341150', 3),
    ('a3333333-0000-0000-0000-000000000001', '516100', 2),
    ('a3333333-0000-0000-0000-000000000001', '341150', 33);

INSERT INTO accounting_exercise (id, property_id, label, start_date, end_date, status, closed_at, closed_by_user_id, comment, created_date, last_modified_date, version) VALUES
    ('a1111111-0000-0000-0000-000000000020', 'a1111111-0000-0000-0000-000000000001', 'Exercice 2026', '2026-01-01', '2026-12-31', 'OPEN', NULL, NULL, NULL, now(), now(), 0),
    ('a2222221-0000-0000-0000-000000000020', 'a2222221-0000-0000-0000-000000000001', 'Exercice 2026', '2026-01-01', '2026-12-31', 'OPEN', NULL, NULL, NULL, now(), now(), 0),
    ('a2222222-0000-0000-0000-000000000020', 'a2222222-0000-0000-0000-000000000001', 'Exercice 2026', '2026-01-01', '2026-12-31', 'OPEN', NULL, NULL, NULL, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000020', 'a3333333-0000-0000-0000-000000000001', 'Exercice 2026', '2026-01-01', '2026-12-31', 'OPEN', NULL, NULL, NULL, now(), now(), 0);

-- Monthly OPEN periods, matching what a real OpenAccountingExerciseService call
-- would generate for each exercise above.
INSERT INTO period (id, exercise_id, year_month, status, created_date, last_modified_date, version) VALUES
    ('7f3e1fb8-ad12-4336-8eec-8ca4bbb29abd', 'a1111111-0000-0000-0000-000000000020', '2026-01-01', 'OPEN', now(), now(), 0),
    ('800115c9-e35e-45a0-9d6d-625d1590296c', 'a1111111-0000-0000-0000-000000000020', '2026-02-01', 'OPEN', now(), now(), 0),
    ('17a644cf-1b29-4c92-994f-e5141b4613e2', 'a1111111-0000-0000-0000-000000000020', '2026-03-01', 'OPEN', now(), now(), 0),
    ('6acf1c69-ddcb-47f4-b621-e1b2ec3dcdc2', 'a1111111-0000-0000-0000-000000000020', '2026-04-01', 'OPEN', now(), now(), 0),
    ('2b9dda5c-aa50-45bb-8a82-92a0eb6bb1e0', 'a1111111-0000-0000-0000-000000000020', '2026-05-01', 'OPEN', now(), now(), 0),
    ('ad446dd0-da04-4f16-b132-7646e03e8765', 'a1111111-0000-0000-0000-000000000020', '2026-06-01', 'OPEN', now(), now(), 0),
    ('b35ec1c0-f808-4c31-ad36-405a9db2ec49', 'a1111111-0000-0000-0000-000000000020', '2026-07-01', 'OPEN', now(), now(), 0),
    ('169c843f-adba-4678-9d7e-7b334c919a98', 'a1111111-0000-0000-0000-000000000020', '2026-08-01', 'OPEN', now(), now(), 0),
    ('044cf55b-59d2-4689-97a7-b16896e48468', 'a1111111-0000-0000-0000-000000000020', '2026-09-01', 'OPEN', now(), now(), 0),
    ('0e5797b5-76de-443f-bffc-4fb54f78f75c', 'a1111111-0000-0000-0000-000000000020', '2026-10-01', 'OPEN', now(), now(), 0),
    ('0f9b6d3c-5f4d-4645-8d1f-280b0950ad82', 'a1111111-0000-0000-0000-000000000020', '2026-11-01', 'OPEN', now(), now(), 0),
    ('44cbaa7c-396d-4643-966b-0bc62b9837a1', 'a1111111-0000-0000-0000-000000000020', '2026-12-01', 'OPEN', now(), now(), 0),
    ('26743efe-c675-4579-8cda-e1bbc50ca4ec', 'a2222221-0000-0000-0000-000000000020', '2026-01-01', 'OPEN', now(), now(), 0),
    ('a162cf95-3c37-4847-bf3c-23f1d44fa0b8', 'a2222221-0000-0000-0000-000000000020', '2026-02-01', 'OPEN', now(), now(), 0),
    ('0d069176-422c-4505-8699-4592c7b8c547', 'a2222221-0000-0000-0000-000000000020', '2026-03-01', 'OPEN', now(), now(), 0),
    ('1e2fc982-0223-4ec6-8889-6d2e8e43d2b6', 'a2222221-0000-0000-0000-000000000020', '2026-04-01', 'OPEN', now(), now(), 0),
    ('a4a64e72-3c94-4b05-b944-2a06d605e1fd', 'a2222221-0000-0000-0000-000000000020', '2026-05-01', 'OPEN', now(), now(), 0),
    ('fdf1f703-09e8-4729-9f42-fd41fd7c8407', 'a2222221-0000-0000-0000-000000000020', '2026-06-01', 'OPEN', now(), now(), 0),
    ('ab3a3f6e-6940-40a8-b5fb-d05b6bf0820a', 'a2222221-0000-0000-0000-000000000020', '2026-07-01', 'OPEN', now(), now(), 0),
    ('ba04cd61-9c01-4925-91f8-be1be5bfc4fe', 'a2222221-0000-0000-0000-000000000020', '2026-08-01', 'OPEN', now(), now(), 0),
    ('acb5f40b-d21c-49ac-8d4f-d23c5902e9be', 'a2222221-0000-0000-0000-000000000020', '2026-09-01', 'OPEN', now(), now(), 0),
    ('a89d94d0-1b3d-4b3b-8c42-0d03d27cb3ef', 'a2222221-0000-0000-0000-000000000020', '2026-10-01', 'OPEN', now(), now(), 0),
    ('a746154e-5a90-45f4-a9ec-bfdbd74afd8f', 'a2222221-0000-0000-0000-000000000020', '2026-11-01', 'OPEN', now(), now(), 0),
    ('bb806355-f475-4afa-91c8-b79e6643246a', 'a2222221-0000-0000-0000-000000000020', '2026-12-01', 'OPEN', now(), now(), 0),
    ('b3b37c8e-8052-4df4-b787-a28035010bed', 'a2222222-0000-0000-0000-000000000020', '2026-01-01', 'OPEN', now(), now(), 0),
    ('ef2a73ca-95e7-45c4-ae1e-5783d88ceacf', 'a2222222-0000-0000-0000-000000000020', '2026-02-01', 'OPEN', now(), now(), 0),
    ('5b360690-43be-424a-8b55-944ef26360a5', 'a2222222-0000-0000-0000-000000000020', '2026-03-01', 'OPEN', now(), now(), 0),
    ('5b209393-6324-4e2d-9c0f-386ded7ec16f', 'a2222222-0000-0000-0000-000000000020', '2026-04-01', 'OPEN', now(), now(), 0),
    ('e6511c2a-6cf5-4cde-9c57-836be05ea96c', 'a2222222-0000-0000-0000-000000000020', '2026-05-01', 'OPEN', now(), now(), 0),
    ('c17ead11-86ba-423d-8380-d0b1abbc2160', 'a2222222-0000-0000-0000-000000000020', '2026-06-01', 'OPEN', now(), now(), 0),
    ('af2ae9ca-e4b2-4b7c-b1e9-bfd25692171a', 'a2222222-0000-0000-0000-000000000020', '2026-07-01', 'OPEN', now(), now(), 0),
    ('044d915a-7821-414c-b5ba-ab168365258a', 'a2222222-0000-0000-0000-000000000020', '2026-08-01', 'OPEN', now(), now(), 0),
    ('09451134-016c-4f8f-afb7-5a1baf017e18', 'a2222222-0000-0000-0000-000000000020', '2026-09-01', 'OPEN', now(), now(), 0),
    ('809c8444-c86e-4f8e-b2b7-d96b07b1af80', 'a2222222-0000-0000-0000-000000000020', '2026-10-01', 'OPEN', now(), now(), 0),
    ('92bf042c-9853-4dd5-8578-f985ba12f0c2', 'a2222222-0000-0000-0000-000000000020', '2026-11-01', 'OPEN', now(), now(), 0),
    ('a32847a6-f046-4207-965c-ef53179646d0', 'a2222222-0000-0000-0000-000000000020', '2026-12-01', 'OPEN', now(), now(), 0),
    ('680eb09d-6805-49e2-8619-2b4b760fd4ab', 'a3333333-0000-0000-0000-000000000020', '2026-01-01', 'OPEN', now(), now(), 0),
    ('74954011-ed91-439e-bd7d-36df8f8a1d19', 'a3333333-0000-0000-0000-000000000020', '2026-02-01', 'OPEN', now(), now(), 0),
    ('c9bf3fe9-b2be-44d5-9342-29e22bc43249', 'a3333333-0000-0000-0000-000000000020', '2026-03-01', 'OPEN', now(), now(), 0),
    ('0665406c-eed5-43b6-8502-0438bb3a5be3', 'a3333333-0000-0000-0000-000000000020', '2026-04-01', 'OPEN', now(), now(), 0),
    ('52bb6f46-2445-474b-89ba-1f6d123b6da1', 'a3333333-0000-0000-0000-000000000020', '2026-05-01', 'OPEN', now(), now(), 0),
    ('06bd7811-2cdf-48a0-9199-87175758e430', 'a3333333-0000-0000-0000-000000000020', '2026-06-01', 'OPEN', now(), now(), 0),
    ('16bbaa79-b1bf-420c-8367-24163abf55c5', 'a3333333-0000-0000-0000-000000000020', '2026-07-01', 'OPEN', now(), now(), 0),
    ('5a7e7794-09cf-40fd-a187-333fa6cab9fb', 'a3333333-0000-0000-0000-000000000020', '2026-08-01', 'OPEN', now(), now(), 0),
    ('58148f39-9a07-4c29-80c5-1f2867117472', 'a3333333-0000-0000-0000-000000000020', '2026-09-01', 'OPEN', now(), now(), 0),
    ('52e4593c-040d-4a99-8296-43c9f4bdeb7a', 'a3333333-0000-0000-0000-000000000020', '2026-10-01', 'OPEN', now(), now(), 0),
    ('9bc2e288-7051-4f43-b7f0-0a3a16a99237', 'a3333333-0000-0000-0000-000000000020', '2026-11-01', 'OPEN', now(), now(), 0),
    ('b575ef30-dfb8-4b8c-9aa2-29494834c2ee', 'a3333333-0000-0000-0000-000000000020', '2026-12-01', 'OPEN', now(), now(), 0);
