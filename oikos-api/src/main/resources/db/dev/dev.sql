-- =========================================================================
-- dev-only: minimal dataset covering user/auth, property (properties,
-- buildings, units, ownerships, board), party and accounting (exercise,
-- financial account, unit account) together, so the whole application can
-- be explored manually (Swagger UI, H2 console) without going through the
-- use cases first.
--
-- Reduced on purpose to exactly the 8 ownership/board-membership profiles
-- below - nothing else. Each CASE section maps 1:1 to one profile:
--   CASE 1 - user1@oikos.com  : 1 lot, 1 résidence (Al Amal), no board seat.
--   CASE 2 - user2@oikos.com  : 1 lot, 1 résidence (Al Amal), + board seat
--                                (Président) in that résidence.
--   CASE 3 - user3@oikos.com  : several lots, 1 résidence (Al Amal), no
--                                board seat.
--   CASE 4 - user4@oikos.com  : several lots, 1 résidence (Al Amal), +
--                                board seat (Trésorier) in that résidence.
--   CASE 5 - user5@oikos.com  : several lots across 2 résidences (Al Amal +
--                                Safae), no board seat anywhere.
--   CASE 6 - user6@oikos.com  : several lots across 2 résidences, + board
--                                seat (Président) in one of them (Safae).
--   CASE 7 - user7@oikos.com  : several lots across 2 résidences, + board
--                                seat in both (Trésorier in Al Amal,
--                                Président in Safae).
--   CASE 8 - user8@oikos.com  : board seat (Gérant/PROPERTY_BOARD_ADMIN) in
--                                2 résidences (Safae + Nour), owns no lot
--                                anywhere - the exact scenario the removal
--                                of the "1 property per volunteer syndic"
--                                cap (EnforcePropertyCreationLimitService)
--                                was meant to unblock.
--
-- Accounts seeded here (every account below shares the same password, Oikos@2026).
-- full_name is a display persona (Moroccan names, mostly, with a couple of
-- French ones for variety) - the login identifier is always the email below,
-- never the name:
--   admin@oikos.com  - system administrator (ROLE_MASTER + ROLE_ADMIN)
--   user1@oikos.com  - CASE 1 - Karim Benali
--   user2@oikos.com  - CASE 2 - Salma Idrissi
--   user3@oikos.com  - CASE 3 - Youssef Alaoui
--   user4@oikos.com  - CASE 4 - Nadia Cherkaoui
--   user5@oikos.com  - CASE 5 - Rachid Tazi
--   user6@oikos.com  - CASE 6 - Camille Rousseau
--   user7@oikos.com  - CASE 7 - Hicham Sefrioui
--   user8@oikos.com  - CASE 8 - Sophie Bernard
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
-- 2. RÉSIDENCE AL AMAL (property 'a1000000-...-0001'): 1 building, 1 unit
-- type (Appartement, price 200), 9 units - just enough lots to cover the
-- ownership counts needed by CASE 1/2/3/4 (1+1+2+2) plus one lot each for
-- the CASE 5/6/7 users who also own here. Owners/board assigned in their
-- respective CASE sections below.
-- =========================================================================

INSERT INTO property (id, name, address, dues_calculation_mode, created_date, last_modified_date, version) VALUES
    ('a1000000-0000-0000-0000-000000000001', 'Résidence Al Amal', '12 rue des Orangers, Casablanca', 'FLAT_RATE', now(), now(), 0);

INSERT INTO building (id, property_id, name, floor_count, created_date, last_modified_date, version) VALUES
    ('a1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 'Bâtiment 1', 3, now(), now(), 0);

INSERT INTO unit_type_definition (id, property_id, name, created_date, last_modified_date, version) VALUES
    ('a1000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000001', 'Appartement', now(), now(), 0);

INSERT INTO unit_type_pricing (id, property_id, unit_type_id, price, created_date, last_modified_date, version) VALUES
    ('a1000000-0000-0000-0000-000000000011', 'a1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000010', 200.00, now(), now(), 0);

INSERT INTO unit (id, building_id, property_id, unit_number, unit_type_id, shares, created_date, last_modified_date, version) VALUES
    ('a1000000-0000-0000-0000-000000000021', 'a1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 'Appartement 1', 'a1000000-0000-0000-0000-000000000010', 100.00, now(), now(), 0),
    ('a1000000-0000-0000-0000-000000000022', 'a1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 'Appartement 2', 'a1000000-0000-0000-0000-000000000010', 100.00, now(), now(), 0),
    ('a1000000-0000-0000-0000-000000000023', 'a1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 'Appartement 3', 'a1000000-0000-0000-0000-000000000010', 100.00, now(), now(), 0),
    ('a1000000-0000-0000-0000-000000000024', 'a1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 'Appartement 4', 'a1000000-0000-0000-0000-000000000010', 100.00, now(), now(), 0),
    ('a1000000-0000-0000-0000-000000000025', 'a1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 'Appartement 5', 'a1000000-0000-0000-0000-000000000010', 100.00, now(), now(), 0),
    ('a1000000-0000-0000-0000-000000000026', 'a1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 'Appartement 6', 'a1000000-0000-0000-0000-000000000010', 100.00, now(), now(), 0),
    ('a1000000-0000-0000-0000-000000000027', 'a1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 'Appartement 7', 'a1000000-0000-0000-0000-000000000010', 100.00, now(), now(), 0),
    ('a1000000-0000-0000-0000-000000000028', 'a1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 'Appartement 8', 'a1000000-0000-0000-0000-000000000010', 100.00, now(), now(), 0),
    ('a1000000-0000-0000-0000-000000000029', 'a1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 'Appartement 9', 'a1000000-0000-0000-0000-000000000010', 100.00, now(), now(), 0);

-- =========================================================================
-- 3. RÉSIDENCE SAFAE (property 'a2000000-...-0001'): same configuration,
-- 3 units - one each for the CASE 5/6/7 users, who also own a lot in Al
-- Amal above. CASE 8's board seat (Gérant) is also granted here.
-- =========================================================================

INSERT INTO property (id, name, address, dues_calculation_mode, created_date, last_modified_date, version) VALUES
    ('a2000000-0000-0000-0000-000000000001', 'Résidence Safae', '5 avenue Mohammed V, Rabat', 'FLAT_RATE', now(), now(), 0);

INSERT INTO building (id, property_id, name, floor_count, created_date, last_modified_date, version) VALUES
    ('a2000000-0000-0000-0000-000000000002', 'a2000000-0000-0000-0000-000000000001', 'Bâtiment 1', 3, now(), now(), 0);

INSERT INTO unit_type_definition (id, property_id, name, created_date, last_modified_date, version) VALUES
    ('a2000000-0000-0000-0000-000000000010', 'a2000000-0000-0000-0000-000000000001', 'Appartement', now(), now(), 0);

INSERT INTO unit_type_pricing (id, property_id, unit_type_id, price, created_date, last_modified_date, version) VALUES
    ('a2000000-0000-0000-0000-000000000011', 'a2000000-0000-0000-0000-000000000001', 'a2000000-0000-0000-0000-000000000010', 200.00, now(), now(), 0);

INSERT INTO unit (id, building_id, property_id, unit_number, unit_type_id, shares, created_date, last_modified_date, version) VALUES
    ('a2000000-0000-0000-0000-000000000021', 'a2000000-0000-0000-0000-000000000002', 'a2000000-0000-0000-0000-000000000001', 'Appartement 1', 'a2000000-0000-0000-0000-000000000010', 100.00, now(), now(), 0),
    ('a2000000-0000-0000-0000-000000000022', 'a2000000-0000-0000-0000-000000000002', 'a2000000-0000-0000-0000-000000000001', 'Appartement 2', 'a2000000-0000-0000-0000-000000000010', 100.00, now(), now(), 0),
    ('a2000000-0000-0000-0000-000000000023', 'a2000000-0000-0000-0000-000000000002', 'a2000000-0000-0000-0000-000000000001', 'Appartement 3', 'a2000000-0000-0000-0000-000000000010', 100.00, now(), now(), 0);

-- =========================================================================
-- 4. RÉSIDENCE NOUR (property 'a3000000-...-0001'): same configuration, 2
-- units, both unowned - this résidence exists purely to hold CASE 8's
-- second board seat (Gérant), with no owner in this dataset.
-- =========================================================================

INSERT INTO property (id, name, address, dues_calculation_mode, created_date, last_modified_date, version) VALUES
    ('a3000000-0000-0000-0000-000000000001', 'Résidence Nour', '8 boulevard Zerktouni, Marrakech', 'FLAT_RATE', now(), now(), 0);

INSERT INTO building (id, property_id, name, floor_count, created_date, last_modified_date, version) VALUES
    ('a3000000-0000-0000-0000-000000000002', 'a3000000-0000-0000-0000-000000000001', 'Bâtiment 1', 3, now(), now(), 0);

INSERT INTO unit_type_definition (id, property_id, name, created_date, last_modified_date, version) VALUES
    ('a3000000-0000-0000-0000-000000000010', 'a3000000-0000-0000-0000-000000000001', 'Appartement', now(), now(), 0);

INSERT INTO unit_type_pricing (id, property_id, unit_type_id, price, created_date, last_modified_date, version) VALUES
    ('a3000000-0000-0000-0000-000000000011', 'a3000000-0000-0000-0000-000000000001', 'a3000000-0000-0000-0000-000000000010', 200.00, now(), now(), 0);

INSERT INTO unit (id, building_id, property_id, unit_number, unit_type_id, shares, created_date, last_modified_date, version) VALUES
    ('a3000000-0000-0000-0000-000000000021', 'a3000000-0000-0000-0000-000000000002', 'a3000000-0000-0000-0000-000000000001', 'Appartement 1', 'a3000000-0000-0000-0000-000000000010', 100.00, now(), now(), 0),
    ('a3000000-0000-0000-0000-000000000022', 'a3000000-0000-0000-0000-000000000002', 'a3000000-0000-0000-0000-000000000001', 'Appartement 2', 'a3000000-0000-0000-0000-000000000010', 100.00, now(), now(), 0);

-- =========================================================================
-- 5. CASE 1 - user1@oikos.com: 1 lot, 1 résidence (Al Amal), no board seat.
-- =========================================================================

INSERT INTO party (id, property_id, full_name, party_type, email, phone, created_date, last_modified_date, version) VALUES
    ('b1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001', 'Karim Benali', 'INDIVIDUAL', 'user1.party@oikos.com', NULL, now(), now(), 0);

-- Password hash below is BCrypt("Oikos@2026"), same algorithm/strength as above.
INSERT INTO app_user (id, email, full_name, password_hash, verified, enabled, created_date, last_modified_date, version) VALUES
    ('b1000000-0000-0000-0000-000000000001', 'user1@oikos.com', 'Karim Benali',
     '$2b$10$jpckGCVGgZLfqalSSjZBhume5BA3lUn2WRFJfQwMgQNb2oPtrJoOu', TRUE, TRUE, now(), now(), 0);

INSERT INTO user_role (user_id, role) VALUES
    ('b1000000-0000-0000-0000-000000000001', 'ROLE_USER');

INSERT INTO app_user_party (app_user_id, party_id) VALUES
    ('b1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000001');

INSERT INTO app_user_party_role (app_user_id, party_id, property_id, role) VALUES
    ('b1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001', 'PROPERTY_OWNER');

INSERT INTO unit_ownership (id, unit_id, party_id, property_id, ownership_share, created_date, last_modified_date, version) VALUES
    ('b1000000-0000-0000-0000-000000000091', 'a1000000-0000-0000-0000-000000000021', 'b1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001', 100.00, now(), now(), 0);

-- =========================================================================
-- 6. CASE 2 - user2@oikos.com: 1 lot, 1 résidence (Al Amal), + board seat
-- (Président) in that résidence.
-- =========================================================================

INSERT INTO party (id, property_id, full_name, party_type, email, phone, created_date, last_modified_date, version) VALUES
    ('b1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 'Salma Idrissi', 'INDIVIDUAL', 'user2.party@oikos.com', NULL, now(), now(), 0);

INSERT INTO app_user (id, email, full_name, password_hash, verified, enabled, created_date, last_modified_date, version) VALUES
    ('b1000000-0000-0000-0000-000000000002', 'user2@oikos.com', 'Salma Idrissi',
     '$2b$10$jpckGCVGgZLfqalSSjZBhume5BA3lUn2WRFJfQwMgQNb2oPtrJoOu', TRUE, TRUE, now(), now(), 0);

INSERT INTO user_role (user_id, role) VALUES
    ('b1000000-0000-0000-0000-000000000002', 'ROLE_USER');

INSERT INTO app_user_party (app_user_id, party_id) VALUES
    ('b1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000002');

INSERT INTO app_user_party_role (app_user_id, party_id, property_id, role) VALUES
    ('b1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 'PROPERTY_OWNER'),
    ('b1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 'PROPERTY_BOARD_MEMBER');

INSERT INTO unit_ownership (id, unit_id, party_id, property_id, ownership_share, created_date, last_modified_date, version) VALUES
    ('b1000000-0000-0000-0000-000000000092', 'a1000000-0000-0000-0000-000000000022', 'b1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 100.00, now(), now(), 0);

INSERT INTO board_member (id, property_id, party_id, board_role, status, created_date, last_modified_date, version) VALUES
    ('b1000000-0000-0000-0000-000000000082', 'a1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000002', 'PRESIDENT', 'ACTIVE', now(), now(), 0);

-- =========================================================================
-- 7. CASE 3 - user3@oikos.com: plusieurs lots (2), 1 résidence (Al Amal),
-- no board seat.
-- =========================================================================

INSERT INTO party (id, property_id, full_name, party_type, email, phone, created_date, last_modified_date, version) VALUES
    ('b1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000001', 'Youssef Alaoui', 'INDIVIDUAL', 'user3.party@oikos.com', NULL, now(), now(), 0);

INSERT INTO app_user (id, email, full_name, password_hash, verified, enabled, created_date, last_modified_date, version) VALUES
    ('b1000000-0000-0000-0000-000000000003', 'user3@oikos.com', 'Youssef Alaoui',
     '$2b$10$jpckGCVGgZLfqalSSjZBhume5BA3lUn2WRFJfQwMgQNb2oPtrJoOu', TRUE, TRUE, now(), now(), 0);

INSERT INTO user_role (user_id, role) VALUES
    ('b1000000-0000-0000-0000-000000000003', 'ROLE_USER');

INSERT INTO app_user_party (app_user_id, party_id) VALUES
    ('b1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000003');

INSERT INTO app_user_party_role (app_user_id, party_id, property_id, role) VALUES
    ('b1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000001', 'PROPERTY_OWNER');

INSERT INTO unit_ownership (id, unit_id, party_id, property_id, ownership_share, created_date, last_modified_date, version) VALUES
    ('b1000000-0000-0000-0000-000000000093', 'a1000000-0000-0000-0000-000000000023', 'b1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000001', 100.00, now(), now(), 0),
    ('b1000000-0000-0000-0000-000000000094', 'a1000000-0000-0000-0000-000000000024', 'b1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000001', 100.00, now(), now(), 0);

-- =========================================================================
-- 8. CASE 4 - user4@oikos.com: plusieurs lots (2), 1 résidence (Al Amal),
-- + board seat (Trésorier) in that résidence.
-- =========================================================================

INSERT INTO party (id, property_id, full_name, party_type, email, phone, created_date, last_modified_date, version) VALUES
    ('b1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000001', 'Nadia Cherkaoui', 'INDIVIDUAL', 'user4.party@oikos.com', NULL, now(), now(), 0);

INSERT INTO app_user (id, email, full_name, password_hash, verified, enabled, created_date, last_modified_date, version) VALUES
    ('b1000000-0000-0000-0000-000000000004', 'user4@oikos.com', 'Nadia Cherkaoui',
     '$2b$10$jpckGCVGgZLfqalSSjZBhume5BA3lUn2WRFJfQwMgQNb2oPtrJoOu', TRUE, TRUE, now(), now(), 0);

INSERT INTO user_role (user_id, role) VALUES
    ('b1000000-0000-0000-0000-000000000004', 'ROLE_USER');

INSERT INTO app_user_party (app_user_id, party_id) VALUES
    ('b1000000-0000-0000-0000-000000000004', 'b1000000-0000-0000-0000-000000000004');

INSERT INTO app_user_party_role (app_user_id, party_id, property_id, role) VALUES
    ('b1000000-0000-0000-0000-000000000004', 'b1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000001', 'PROPERTY_OWNER'),
    ('b1000000-0000-0000-0000-000000000004', 'b1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000001', 'PROPERTY_BOARD_MEMBER');

INSERT INTO unit_ownership (id, unit_id, party_id, property_id, ownership_share, created_date, last_modified_date, version) VALUES
    ('b1000000-0000-0000-0000-000000000095', 'a1000000-0000-0000-0000-000000000025', 'b1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000001', 100.00, now(), now(), 0),
    ('b1000000-0000-0000-0000-000000000096', 'a1000000-0000-0000-0000-000000000026', 'b1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000001', 100.00, now(), now(), 0);

INSERT INTO board_member (id, property_id, party_id, board_role, status, created_date, last_modified_date, version) VALUES
    ('b1000000-0000-0000-0000-000000000084', 'a1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000004', 'TREASURER', 'ACTIVE', now(), now(), 0);

-- =========================================================================
-- 9. CASE 5 - user5@oikos.com: plusieurs lots dans plusieurs résidences (Al
-- Amal + Safae), no board seat anywhere. A party belongs to exactly one
-- property, so user5 gets one party per résidence, both linked to the same
-- app_user via app_user_party (same pattern as any multi-property account).
-- =========================================================================

INSERT INTO party (id, property_id, full_name, party_type, email, phone, created_date, last_modified_date, version) VALUES
    ('a1000000-0000-0000-0000-000000000105', 'a1000000-0000-0000-0000-000000000001', 'Rachid Tazi (Al Amal)', 'INDIVIDUAL', 'user5.alamal.party@oikos.com', NULL, now(), now(), 0),
    ('a2000000-0000-0000-0000-000000000105', 'a2000000-0000-0000-0000-000000000001', 'Rachid Tazi (Safae)', 'INDIVIDUAL', 'user5.safae.party@oikos.com', NULL, now(), now(), 0);

INSERT INTO app_user (id, email, full_name, password_hash, verified, enabled, created_date, last_modified_date, version) VALUES
    ('b2000000-0000-0000-0000-000000000005', 'user5@oikos.com', 'Rachid Tazi',
     '$2b$10$jpckGCVGgZLfqalSSjZBhume5BA3lUn2WRFJfQwMgQNb2oPtrJoOu', TRUE, TRUE, now(), now(), 0);

INSERT INTO user_role (user_id, role) VALUES
    ('b2000000-0000-0000-0000-000000000005', 'ROLE_USER');

INSERT INTO app_user_party (app_user_id, party_id) VALUES
    ('b2000000-0000-0000-0000-000000000005', 'a1000000-0000-0000-0000-000000000105'),
    ('b2000000-0000-0000-0000-000000000005', 'a2000000-0000-0000-0000-000000000105');

INSERT INTO app_user_party_role (app_user_id, party_id, property_id, role) VALUES
    ('b2000000-0000-0000-0000-000000000005', 'a1000000-0000-0000-0000-000000000105', 'a1000000-0000-0000-0000-000000000001', 'PROPERTY_OWNER'),
    ('b2000000-0000-0000-0000-000000000005', 'a2000000-0000-0000-0000-000000000105', 'a2000000-0000-0000-0000-000000000001', 'PROPERTY_OWNER');

INSERT INTO unit_ownership (id, unit_id, party_id, property_id, ownership_share, created_date, last_modified_date, version) VALUES
    ('b2000000-0000-0000-0000-000000000097', 'a1000000-0000-0000-0000-000000000027', 'a1000000-0000-0000-0000-000000000105', 'a1000000-0000-0000-0000-000000000001', 100.00, now(), now(), 0),
    ('b2000000-0000-0000-0000-000000000098', 'a2000000-0000-0000-0000-000000000021', 'a2000000-0000-0000-0000-000000000105', 'a2000000-0000-0000-0000-000000000001', 100.00, now(), now(), 0);

-- =========================================================================
-- 10. CASE 6 - user6@oikos.com: plusieurs lots dans plusieurs résidences (Al
-- Amal + Safae), + board seat (Président) in one of them (Safae).
-- =========================================================================

INSERT INTO party (id, property_id, full_name, party_type, email, phone, created_date, last_modified_date, version) VALUES
    ('a1000000-0000-0000-0000-000000000106', 'a1000000-0000-0000-0000-000000000001', 'Camille Rousseau (Al Amal)', 'INDIVIDUAL', 'user6.alamal.party@oikos.com', NULL, now(), now(), 0),
    ('a2000000-0000-0000-0000-000000000106', 'a2000000-0000-0000-0000-000000000001', 'Camille Rousseau (Safae)', 'INDIVIDUAL', 'user6.safae.party@oikos.com', NULL, now(), now(), 0);

INSERT INTO app_user (id, email, full_name, password_hash, verified, enabled, created_date, last_modified_date, version) VALUES
    ('b2000000-0000-0000-0000-000000000006', 'user6@oikos.com', 'Camille Rousseau',
     '$2b$10$jpckGCVGgZLfqalSSjZBhume5BA3lUn2WRFJfQwMgQNb2oPtrJoOu', TRUE, TRUE, now(), now(), 0);

INSERT INTO user_role (user_id, role) VALUES
    ('b2000000-0000-0000-0000-000000000006', 'ROLE_USER');

INSERT INTO app_user_party (app_user_id, party_id) VALUES
    ('b2000000-0000-0000-0000-000000000006', 'a1000000-0000-0000-0000-000000000106'),
    ('b2000000-0000-0000-0000-000000000006', 'a2000000-0000-0000-0000-000000000106');

INSERT INTO app_user_party_role (app_user_id, party_id, property_id, role) VALUES
    ('b2000000-0000-0000-0000-000000000006', 'a1000000-0000-0000-0000-000000000106', 'a1000000-0000-0000-0000-000000000001', 'PROPERTY_OWNER'),
    ('b2000000-0000-0000-0000-000000000006', 'a2000000-0000-0000-0000-000000000106', 'a2000000-0000-0000-0000-000000000001', 'PROPERTY_OWNER'),
    ('b2000000-0000-0000-0000-000000000006', 'a2000000-0000-0000-0000-000000000106', 'a2000000-0000-0000-0000-000000000001', 'PROPERTY_BOARD_MEMBER');

INSERT INTO unit_ownership (id, unit_id, party_id, property_id, ownership_share, created_date, last_modified_date, version) VALUES
    ('b2000000-0000-0000-0000-000000000099', 'a1000000-0000-0000-0000-000000000028', 'a1000000-0000-0000-0000-000000000106', 'a1000000-0000-0000-0000-000000000001', 100.00, now(), now(), 0),
    ('b2000000-0000-0000-0000-00000000009a', 'a2000000-0000-0000-0000-000000000022', 'a2000000-0000-0000-0000-000000000106', 'a2000000-0000-0000-0000-000000000001', 100.00, now(), now(), 0);

INSERT INTO board_member (id, property_id, party_id, board_role, status, created_date, last_modified_date, version) VALUES
    ('b2000000-0000-0000-0000-000000000086', 'a2000000-0000-0000-0000-000000000001', 'a2000000-0000-0000-0000-000000000106', 'PRESIDENT', 'ACTIVE', now(), now(), 0);

-- =========================================================================
-- 11. CASE 7 - user7@oikos.com: plusieurs lots dans plusieurs résidences (Al
-- Amal + Safae), + board seat in both (Trésorier in Al Amal, Président in
-- Safae).
-- =========================================================================

INSERT INTO party (id, property_id, full_name, party_type, email, phone, created_date, last_modified_date, version) VALUES
    ('a1000000-0000-0000-0000-000000000107', 'a1000000-0000-0000-0000-000000000001', 'Hicham Sefrioui (Al Amal)', 'INDIVIDUAL', 'user7.alamal.party@oikos.com', NULL, now(), now(), 0),
    ('a2000000-0000-0000-0000-000000000107', 'a2000000-0000-0000-0000-000000000001', 'Hicham Sefrioui (Safae)', 'INDIVIDUAL', 'user7.safae.party@oikos.com', NULL, now(), now(), 0);

INSERT INTO app_user (id, email, full_name, password_hash, verified, enabled, created_date, last_modified_date, version) VALUES
    ('b2000000-0000-0000-0000-000000000007', 'user7@oikos.com', 'Hicham Sefrioui',
     '$2b$10$jpckGCVGgZLfqalSSjZBhume5BA3lUn2WRFJfQwMgQNb2oPtrJoOu', TRUE, TRUE, now(), now(), 0);

INSERT INTO user_role (user_id, role) VALUES
    ('b2000000-0000-0000-0000-000000000007', 'ROLE_USER');

INSERT INTO app_user_party (app_user_id, party_id) VALUES
    ('b2000000-0000-0000-0000-000000000007', 'a1000000-0000-0000-0000-000000000107'),
    ('b2000000-0000-0000-0000-000000000007', 'a2000000-0000-0000-0000-000000000107');

INSERT INTO app_user_party_role (app_user_id, party_id, property_id, role) VALUES
    ('b2000000-0000-0000-0000-000000000007', 'a1000000-0000-0000-0000-000000000107', 'a1000000-0000-0000-0000-000000000001', 'PROPERTY_OWNER'),
    ('b2000000-0000-0000-0000-000000000007', 'a1000000-0000-0000-0000-000000000107', 'a1000000-0000-0000-0000-000000000001', 'PROPERTY_BOARD_MEMBER'),
    ('b2000000-0000-0000-0000-000000000007', 'a2000000-0000-0000-0000-000000000107', 'a2000000-0000-0000-0000-000000000001', 'PROPERTY_OWNER'),
    ('b2000000-0000-0000-0000-000000000007', 'a2000000-0000-0000-0000-000000000107', 'a2000000-0000-0000-0000-000000000001', 'PROPERTY_BOARD_MEMBER');

INSERT INTO unit_ownership (id, unit_id, party_id, property_id, ownership_share, created_date, last_modified_date, version) VALUES
    ('b2000000-0000-0000-0000-00000000009b', 'a1000000-0000-0000-0000-000000000029', 'a1000000-0000-0000-0000-000000000107', 'a1000000-0000-0000-0000-000000000001', 100.00, now(), now(), 0),
    ('b2000000-0000-0000-0000-00000000009c', 'a2000000-0000-0000-0000-000000000023', 'a2000000-0000-0000-0000-000000000107', 'a2000000-0000-0000-0000-000000000001', 100.00, now(), now(), 0);

INSERT INTO board_member (id, property_id, party_id, board_role, status, created_date, last_modified_date, version) VALUES
    ('b2000000-0000-0000-0000-000000000087', 'a1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000107', 'TREASURER', 'ACTIVE', now(), now(), 0),
    ('b2000000-0000-0000-0000-000000000088', 'a2000000-0000-0000-0000-000000000001', 'a2000000-0000-0000-0000-000000000107', 'PRESIDENT', 'ACTIVE', now(), now(), 0);

-- =========================================================================
-- 12. CASE 8 - user8@oikos.com: membre de plusieurs bureaux de syndic dans
-- plusieurs résidences (Gérant/PROPERTY_BOARD_ADMIN in both Safae and
-- Nour), sans être propriétaire nulle part. This is the exact scenario the
-- removal of the "1 property per volunteer syndic" cap
-- (EnforcePropertyCreationLimitService, see PropertyController) was meant
-- to unblock: a single account holding PROPERTY_BOARD_ADMIN on 2 distinct
-- properties.
-- =========================================================================

INSERT INTO party (id, property_id, full_name, party_type, email, phone, created_date, last_modified_date, version) VALUES
    ('a2000000-0000-0000-0000-000000000108', 'a2000000-0000-0000-0000-000000000001', 'Sophie Bernard (Safae)', 'INDIVIDUAL', 'user8.safae.party@oikos.com', '0600000008', now(), now(), 0),
    ('a3000000-0000-0000-0000-000000000108', 'a3000000-0000-0000-0000-000000000001', 'Sophie Bernard (Nour)', 'INDIVIDUAL', 'user8.nour.party@oikos.com', '0600000008', now(), now(), 0);

INSERT INTO app_user (id, email, full_name, password_hash, verified, enabled, created_date, last_modified_date, version) VALUES
    ('b2000000-0000-0000-0000-000000000008', 'user8@oikos.com', 'Sophie Bernard',
     '$2b$10$jpckGCVGgZLfqalSSjZBhume5BA3lUn2WRFJfQwMgQNb2oPtrJoOu', TRUE, TRUE, now(), now(), 0);

INSERT INTO user_role (user_id, role) VALUES
    ('b2000000-0000-0000-0000-000000000008', 'ROLE_USER');

INSERT INTO app_user_party (app_user_id, party_id) VALUES
    ('b2000000-0000-0000-0000-000000000008', 'a2000000-0000-0000-0000-000000000108'),
    ('b2000000-0000-0000-0000-000000000008', 'a3000000-0000-0000-0000-000000000108');

INSERT INTO app_user_party_role (app_user_id, party_id, property_id, role) VALUES
    ('b2000000-0000-0000-0000-000000000008', 'a2000000-0000-0000-0000-000000000108', 'a2000000-0000-0000-0000-000000000001', 'PROPERTY_BOARD_ADMIN'),
    ('b2000000-0000-0000-0000-000000000008', 'a3000000-0000-0000-0000-000000000108', 'a3000000-0000-0000-0000-000000000001', 'PROPERTY_BOARD_ADMIN');

INSERT INTO board_member (id, property_id, party_id, board_role, status, created_date, last_modified_date, version) VALUES
    ('b2000000-0000-0000-0000-000000000089', 'a2000000-0000-0000-0000-000000000001', 'a2000000-0000-0000-0000-000000000108', 'PROPERTY_MANAGER', 'ACTIVE', now(), now(), 0),
    ('b2000000-0000-0000-0000-00000000008a', 'a3000000-0000-0000-0000-000000000001', 'a3000000-0000-0000-0000-000000000108', 'PROPERTY_MANAGER', 'ACTIVE', now(), now(), 0);

-- =========================================================================
-- 13. ACCOUNTING (PCM engine, ADR 0001): the shared "usage syndic" chart of
-- accounts (property_id/unit_id NULL, same rows as the V11 Flyway seed -
-- dev boots via Hibernate ddl-auto=create-drop from the JPA entities, which
-- ignores Flyway entirely, hence the duplication here) plus one open
-- exercise per résidence. Property/unit-specific accounts (caisse, creance
-- par lot - normally provisioned automatically by
-- ProvisionPropertyCashAccountService/ProvisionUnitReceivableAccountService
-- when a real Property/Unit is created through the API) and the monthly
-- OPEN periods a real OpenAccountingExerciseService call would generate are
-- seeded explicitly below instead, since these properties/units/exercises
-- are inserted as raw SQL above, bypassing those flows.
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
    ('a1000000-0000-0000-0000-000000000030', 'a1000000-0000-0000-0000-000000000001', NULL, '51610001', 'Caisse', 5, 'BALANCE_ASSET', FALSE, 'CASH', TRUE, 0, now(), now(), 0),
    ('a1000000-0000-0000-0000-000000000040', 'a1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000021', '34115001', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a1000000-0000-0000-0000-000000000041', 'a1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000022', '34115002', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a1000000-0000-0000-0000-000000000042', 'a1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000023', '34115003', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a1000000-0000-0000-0000-000000000043', 'a1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000024', '34115004', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a1000000-0000-0000-0000-000000000044', 'a1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000025', '34115005', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a1000000-0000-0000-0000-000000000045', 'a1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000026', '34115006', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a1000000-0000-0000-0000-000000000046', 'a1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000027', '34115007', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a1000000-0000-0000-0000-000000000047', 'a1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000028', '34115008', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a1000000-0000-0000-0000-000000000048', 'a1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000029', '34115009', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a2000000-0000-0000-0000-000000000030', 'a2000000-0000-0000-0000-000000000001', NULL, '51610001', 'Caisse', 5, 'BALANCE_ASSET', FALSE, 'CASH', TRUE, 0, now(), now(), 0),
    ('a2000000-0000-0000-0000-000000000040', 'a2000000-0000-0000-0000-000000000001', 'a2000000-0000-0000-0000-000000000021', '34115001', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a2000000-0000-0000-0000-000000000041', 'a2000000-0000-0000-0000-000000000001', 'a2000000-0000-0000-0000-000000000022', '34115002', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a2000000-0000-0000-0000-000000000042', 'a2000000-0000-0000-0000-000000000001', 'a2000000-0000-0000-0000-000000000023', '34115003', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3000000-0000-0000-0000-000000000030', 'a3000000-0000-0000-0000-000000000001', NULL, '51610001', 'Caisse', 5, 'BALANCE_ASSET', FALSE, 'CASH', TRUE, 0, now(), now(), 0),
    ('a3000000-0000-0000-0000-000000000040', 'a3000000-0000-0000-0000-000000000001', 'a3000000-0000-0000-0000-000000000021', '34115001', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0),
    ('a3000000-0000-0000-0000-000000000041', 'a3000000-0000-0000-0000-000000000001', 'a3000000-0000-0000-0000-000000000022', '34115002', 'Coproprietaire - creance', 3, 'BALANCE_ASSET', FALSE, 'UNIT_RECEIVABLE', TRUE, 0, now(), now(), 0);

INSERT INTO ledger_account_number_sequence (property_id, number_prefix, next_increment) VALUES
    ('a1000000-0000-0000-0000-000000000001', '516100', 2),
    ('a1000000-0000-0000-0000-000000000001', '341150', 10),
    ('a2000000-0000-0000-0000-000000000001', '516100', 2),
    ('a2000000-0000-0000-0000-000000000001', '341150', 4),
    ('a3000000-0000-0000-0000-000000000001', '516100', 2),
    ('a3000000-0000-0000-0000-000000000001', '341150', 3);

INSERT INTO accounting_exercise (id, property_id, label, start_date, end_date, status, closed_at, closed_by_user_id, comment, created_date, last_modified_date, version) VALUES
    ('e1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001', 'Exercice 2026', '2026-01-01', '2026-12-31', 'OPEN', NULL, NULL, NULL, now(), now(), 0),
    ('e2000000-0000-0000-0000-000000000001', 'a2000000-0000-0000-0000-000000000001', 'Exercice 2026', '2026-01-01', '2026-12-31', 'OPEN', NULL, NULL, NULL, now(), now(), 0),
    ('e3000000-0000-0000-0000-000000000001', 'a3000000-0000-0000-0000-000000000001', 'Exercice 2026', '2026-01-01', '2026-12-31', 'OPEN', NULL, NULL, NULL, now(), now(), 0);

-- Monthly OPEN periods, matching what a real OpenAccountingExerciseService call
-- would generate for each exercise above.
INSERT INTO period (id, exercise_id, year_month, status, created_date, last_modified_date, version) VALUES
    ('d1000000-0000-0000-0000-000000000001', 'e1000000-0000-0000-0000-000000000001', '2026-01-01', 'OPEN', now(), now(), 0),
    ('d1000000-0000-0000-0000-000000000002', 'e1000000-0000-0000-0000-000000000001', '2026-02-01', 'OPEN', now(), now(), 0),
    ('d1000000-0000-0000-0000-000000000003', 'e1000000-0000-0000-0000-000000000001', '2026-03-01', 'OPEN', now(), now(), 0),
    ('d1000000-0000-0000-0000-000000000004', 'e1000000-0000-0000-0000-000000000001', '2026-04-01', 'OPEN', now(), now(), 0),
    ('d1000000-0000-0000-0000-000000000005', 'e1000000-0000-0000-0000-000000000001', '2026-05-01', 'OPEN', now(), now(), 0),
    ('d1000000-0000-0000-0000-000000000006', 'e1000000-0000-0000-0000-000000000001', '2026-06-01', 'OPEN', now(), now(), 0),
    ('d1000000-0000-0000-0000-000000000007', 'e1000000-0000-0000-0000-000000000001', '2026-07-01', 'OPEN', now(), now(), 0),
    ('d1000000-0000-0000-0000-000000000008', 'e1000000-0000-0000-0000-000000000001', '2026-08-01', 'OPEN', now(), now(), 0),
    ('d1000000-0000-0000-0000-000000000009', 'e1000000-0000-0000-0000-000000000001', '2026-09-01', 'OPEN', now(), now(), 0),
    ('d1000000-0000-0000-0000-000000000010', 'e1000000-0000-0000-0000-000000000001', '2026-10-01', 'OPEN', now(), now(), 0),
    ('d1000000-0000-0000-0000-000000000011', 'e1000000-0000-0000-0000-000000000001', '2026-11-01', 'OPEN', now(), now(), 0),
    ('d1000000-0000-0000-0000-000000000012', 'e1000000-0000-0000-0000-000000000001', '2026-12-01', 'OPEN', now(), now(), 0),
    ('d2000000-0000-0000-0000-000000000001', 'e2000000-0000-0000-0000-000000000001', '2026-01-01', 'OPEN', now(), now(), 0),
    ('d2000000-0000-0000-0000-000000000002', 'e2000000-0000-0000-0000-000000000001', '2026-02-01', 'OPEN', now(), now(), 0),
    ('d2000000-0000-0000-0000-000000000003', 'e2000000-0000-0000-0000-000000000001', '2026-03-01', 'OPEN', now(), now(), 0),
    ('d2000000-0000-0000-0000-000000000004', 'e2000000-0000-0000-0000-000000000001', '2026-04-01', 'OPEN', now(), now(), 0),
    ('d2000000-0000-0000-0000-000000000005', 'e2000000-0000-0000-0000-000000000001', '2026-05-01', 'OPEN', now(), now(), 0),
    ('d2000000-0000-0000-0000-000000000006', 'e2000000-0000-0000-0000-000000000001', '2026-06-01', 'OPEN', now(), now(), 0),
    ('d2000000-0000-0000-0000-000000000007', 'e2000000-0000-0000-0000-000000000001', '2026-07-01', 'OPEN', now(), now(), 0),
    ('d2000000-0000-0000-0000-000000000008', 'e2000000-0000-0000-0000-000000000001', '2026-08-01', 'OPEN', now(), now(), 0),
    ('d2000000-0000-0000-0000-000000000009', 'e2000000-0000-0000-0000-000000000001', '2026-09-01', 'OPEN', now(), now(), 0),
    ('d2000000-0000-0000-0000-000000000010', 'e2000000-0000-0000-0000-000000000001', '2026-10-01', 'OPEN', now(), now(), 0),
    ('d2000000-0000-0000-0000-000000000011', 'e2000000-0000-0000-0000-000000000001', '2026-11-01', 'OPEN', now(), now(), 0),
    ('d2000000-0000-0000-0000-000000000012', 'e2000000-0000-0000-0000-000000000001', '2026-12-01', 'OPEN', now(), now(), 0),
    ('d3000000-0000-0000-0000-000000000001', 'e3000000-0000-0000-0000-000000000001', '2026-01-01', 'OPEN', now(), now(), 0),
    ('d3000000-0000-0000-0000-000000000002', 'e3000000-0000-0000-0000-000000000001', '2026-02-01', 'OPEN', now(), now(), 0),
    ('d3000000-0000-0000-0000-000000000003', 'e3000000-0000-0000-0000-000000000001', '2026-03-01', 'OPEN', now(), now(), 0),
    ('d3000000-0000-0000-0000-000000000004', 'e3000000-0000-0000-0000-000000000001', '2026-04-01', 'OPEN', now(), now(), 0),
    ('d3000000-0000-0000-0000-000000000005', 'e3000000-0000-0000-0000-000000000001', '2026-05-01', 'OPEN', now(), now(), 0),
    ('d3000000-0000-0000-0000-000000000006', 'e3000000-0000-0000-0000-000000000001', '2026-06-01', 'OPEN', now(), now(), 0),
    ('d3000000-0000-0000-0000-000000000007', 'e3000000-0000-0000-0000-000000000001', '2026-07-01', 'OPEN', now(), now(), 0),
    ('d3000000-0000-0000-0000-000000000008', 'e3000000-0000-0000-0000-000000000001', '2026-08-01', 'OPEN', now(), now(), 0),
    ('d3000000-0000-0000-0000-000000000009', 'e3000000-0000-0000-0000-000000000001', '2026-09-01', 'OPEN', now(), now(), 0),
    ('d3000000-0000-0000-0000-000000000010', 'e3000000-0000-0000-0000-000000000001', '2026-10-01', 'OPEN', now(), now(), 0),
    ('d3000000-0000-0000-0000-000000000011', 'e3000000-0000-0000-0000-000000000001', '2026-11-01', 'OPEN', now(), now(), 0),
    ('d3000000-0000-0000-0000-000000000012', 'e3000000-0000-0000-0000-000000000001', '2026-12-01', 'OPEN', now(), now(), 0);
