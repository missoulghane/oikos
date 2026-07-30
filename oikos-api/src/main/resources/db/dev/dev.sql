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

INSERT INTO property (id, name, address, created_date, last_modified_date, version) VALUES
    ('a1111111-0000-0000-0000-000000000001', 'Copro 1', '1 rue de la Paix, Casablanca', now(), now(), 0);

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

INSERT INTO board_member (id, property_id, party_id, board_role, created_date, last_modified_date, version) VALUES
    ('a1111111-0000-0000-0000-000000000006', 'a1111111-0000-0000-0000-000000000001', 'a1111111-0000-0000-0000-000000000005', 'PROPERTY_MANAGER', now(), now(), 0);

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

INSERT INTO property (id, name, address, created_date, last_modified_date, version) VALUES
    ('a2222221-0000-0000-0000-000000000001', 'Copro 2.1', '2 avenue Hassan II, Rabat', now(), now(), 0);

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

INSERT INTO property (id, name, address, created_date, last_modified_date, version) VALUES
    ('a2222222-0000-0000-0000-000000000001', 'Copro 2.2', '3 avenue Hassan II, Rabat', now(), now(), 0);

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

INSERT INTO board_member (id, property_id, party_id, board_role, created_date, last_modified_date, version) VALUES
    ('b0000000-0000-0000-0000-000000000011', 'a2222221-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 'PROPERTY_MANAGER', now(), now(), 0),
    ('b0000000-0000-0000-0000-000000000012', 'a2222222-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000002', 'PROPERTY_MANAGER', now(), now(), 0);

-- =========================================================================
-- 6. COPRO 3 (property 'a3333333-...-0001'): same configuration as Copro 1.
-- Manager: syndic1@oikos.com, a volunteer syndic (PROPERTY_BOARD_ADMIN),
-- unlike manager1/manager2 who are professional firms (PROPERTY_MANAGER_ADMIN).
-- Owners: copr3_user1@oikos.com (unit 1), copr3_user2@oikos.com (unit 2).
-- =========================================================================

INSERT INTO property (id, name, address, created_date, last_modified_date, version) VALUES
    ('a3333333-0000-0000-0000-000000000001', 'Copro 3', '4 avenue Hassan II, Rabat', now(), now(), 0);

INSERT INTO building (id, property_id, name, floor_count, created_date, last_modified_date, version) VALUES
    ('a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Bâtiment 1', 3, now(), now(), 0);

INSERT INTO unit_type_definition (id, property_id, name, created_date, last_modified_date, version) VALUES
    ('a3333333-0000-0000-0000-000000000010', 'a3333333-0000-0000-0000-000000000001', 'Appartement', now(), now(), 0);

INSERT INTO unit_type_pricing (id, property_id, unit_type_id, price, created_date, last_modified_date, version) VALUES
    ('a3333333-0000-0000-0000-000000000011', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000010', 200.00, now(), now(), 0);

INSERT INTO unit (id, building_id, property_id, unit_number, unit_type_id, shares, created_date, last_modified_date, version) VALUES
    ('a3333333-0000-0000-0000-000000000003', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 1', 'a3333333-0000-0000-0000-000000000010', 100.00, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000004', 'a3333333-0000-0000-0000-000000000002', 'a3333333-0000-0000-0000-000000000001', 'Appartement 2', 'a3333333-0000-0000-0000-000000000010', 100.00, now(), now(), 0);

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

INSERT INTO board_member (id, property_id, party_id, board_role, created_date, last_modified_date, version) VALUES
    ('a3333333-0000-0000-0000-000000000006', 'a3333333-0000-0000-0000-000000000001', 'a3333333-0000-0000-0000-000000000005', 'PROPERTY_MANAGER', now(), now(), 0);

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
-- 7. ACCOUNTING (Phase 1): one open exercise + one "Caisse" financial
-- account per copro, and one unit_account per seeded unit (mirrors what
-- CreateUnitAccountService provisions automatically when a real Unit is
-- created through the API - these seeded units are inserted as raw SQL
-- above, bypassing that use case, so their accounts are seeded by hand here
-- to keep the dev dataset consistent with what the app would produce).
-- =========================================================================

INSERT INTO accounting_exercise (id, property_id, label, start_date, end_date, status, closed_at, closed_by_user_id, comment, created_date, last_modified_date, version) VALUES
    ('a1111111-0000-0000-0000-000000000020', 'a1111111-0000-0000-0000-000000000001', 'Exercice 2026', '2026-01-01', '2026-12-31', 'OPEN', NULL, NULL, NULL, now(), now(), 0),
    ('a2222221-0000-0000-0000-000000000020', 'a2222221-0000-0000-0000-000000000001', 'Exercice 2026', '2026-01-01', '2026-12-31', 'OPEN', NULL, NULL, NULL, now(), now(), 0),
    ('a2222222-0000-0000-0000-000000000020', 'a2222222-0000-0000-0000-000000000001', 'Exercice 2026', '2026-01-01', '2026-12-31', 'OPEN', NULL, NULL, NULL, now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000020', 'a3333333-0000-0000-0000-000000000001', 'Exercice 2026', '2026-01-01', '2026-12-31', 'OPEN', NULL, NULL, NULL, now(), now(), 0);

INSERT INTO financial_account (id, property_id, name, type, currency, balance, status, created_date, last_modified_date, version) VALUES
    ('a1111111-0000-0000-0000-000000000021', 'a1111111-0000-0000-0000-000000000001', 'Caisse', 'CASH', 'MAD', 0.00, 'ACTIVE', now(), now(), 0),
    ('a2222221-0000-0000-0000-000000000021', 'a2222221-0000-0000-0000-000000000001', 'Caisse', 'CASH', 'MAD', 0.00, 'ACTIVE', now(), now(), 0),
    ('a2222222-0000-0000-0000-000000000021', 'a2222222-0000-0000-0000-000000000001', 'Caisse', 'CASH', 'MAD', 0.00, 'ACTIVE', now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000021', 'a3333333-0000-0000-0000-000000000001', 'Caisse', 'CASH', 'MAD', 0.00, 'ACTIVE', now(), now(), 0);

INSERT INTO unit_account (id, unit_id, property_id, balance, last_updated_date, created_date, last_modified_date, version) VALUES
    ('a1111111-0000-0000-0000-000000000022', 'a1111111-0000-0000-0000-000000000003', 'a1111111-0000-0000-0000-000000000001', 0.00, now(), now(), now(), 0),
    ('a1111111-0000-0000-0000-000000000023', 'a1111111-0000-0000-0000-000000000004', 'a1111111-0000-0000-0000-000000000001', 0.00, now(), now(), now(), 0),
    ('a2222221-0000-0000-0000-000000000022', 'a2222221-0000-0000-0000-000000000003', 'a2222221-0000-0000-0000-000000000001', 0.00, now(), now(), now(), 0),
    ('a2222221-0000-0000-0000-000000000023', 'a2222221-0000-0000-0000-000000000004', 'a2222221-0000-0000-0000-000000000001', 0.00, now(), now(), now(), 0),
    ('a2222222-0000-0000-0000-000000000022', 'a2222222-0000-0000-0000-000000000003', 'a2222222-0000-0000-0000-000000000001', 0.00, now(), now(), now(), 0),
    ('a2222222-0000-0000-0000-000000000023', 'a2222222-0000-0000-0000-000000000004', 'a2222222-0000-0000-0000-000000000001', 0.00, now(), now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000022', 'a3333333-0000-0000-0000-000000000003', 'a3333333-0000-0000-0000-000000000001', 0.00, now(), now(), now(), 0),
    ('a3333333-0000-0000-0000-000000000023', 'a3333333-0000-0000-0000-000000000004', 'a3333333-0000-0000-0000-000000000001', 0.00, now(), now(), now(), 0);
