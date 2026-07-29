-- =========================================================================
-- V2: fine-grained RBAC. Introduces the atomic permission catalog and the
-- role_permission bundle table (data-driven: a role's bundle is editable
-- here without a code change). Expands the property-scoped role set from
-- 2 (ROLE_PROPERTY_MANAGER / ROLE_PROPERTY_ADMIN, the latter unused) to the
-- 5 business profiles: PROPERTY_BOARD_ADMIN/MEMBER,
-- PROPERTY_MANAGER_ADMIN/MEMBER, PROPERTY_OWNER. app_user_party_role and
-- user_role keep their existing shape (VARCHAR role column) - only the
-- value domain of that column changes, so no entity/schema-shape migration
-- is needed beyond this table + the remap below.
-- =========================================================================

CREATE TABLE permission (
    key         VARCHAR(60) PRIMARY KEY,
    description VARCHAR(200) NOT NULL
);

-- role_name spans BOTH global roles (user_role.role values, e.g. ROLE_ADMIN)
-- and property-scoped roles (app_user_party_role.role values, e.g.
-- PROPERTY_BOARD_ADMIN) - one bundle table for both, since "which
-- permissions does holding this role name grant" is the same question for
-- either.
CREATE TABLE role_permission (
    role_name      VARCHAR(50) NOT NULL,
    permission_key VARCHAR(60) NOT NULL,
    CONSTRAINT pk_role_permission PRIMARY KEY (role_name, permission_key),
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_key) REFERENCES permission (key)
);

INSERT INTO permission (key, description) VALUES
    ('property:read', 'Read a property and its structure'),
    ('property:create', 'Create a new property'),
    ('property:update', 'Update property details'),
    ('property:board:manage', 'Manage board members'),
    ('property:member:invite', 'Invite a board/manager member onto a property'),
    ('unit:read', 'Read units'),
    ('unit:write', 'Create/update units, buildings, unit types, pricing'),
    ('unit:ownership:write', 'Manage unit ownership records'),
    ('party:read', 'Read parties'),
    ('party:write', 'Create/update parties'),
    ('party:invite', 'Invite a party (owner) to link an account'),
    ('installment:read', 'Read installments/installment calls'),
    ('installment:call:write', 'Generate/record installment calls'),
    ('property:accounting:read', 'Read ledger/account balances (future accounting module)'),
    ('property:accounting:write', 'Post ledger movements (future accounting module)'),
    ('user:admin', 'Platform user administration');

-- SYSTEM_ADMIN (global ROLE_ADMIN): the full bundle, kept explicit (rather
-- than only relying on the isAdmin() short-circuit) so it stays
-- inspectable/auditable like every other role.
INSERT INTO role_permission (role_name, permission_key)
SELECT 'ROLE_ADMIN', key FROM permission;

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

-- BOARD_MEMBER: identical bundle at launch, MINUS property:create and
-- property:member:invite (members never create a property nor invite other
-- members) - a distinct row so it can be narrowed further later in DB alone.
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

-- MANAGER_ADMIN / MANAGER_MEMBER: same shape as their BOARD_* counterparts
-- at launch (identical bundles, per the requested profile design).
INSERT INTO role_permission (role_name, permission_key)
SELECT 'PROPERTY_MANAGER_ADMIN', permission_key FROM role_permission WHERE role_name = 'PROPERTY_BOARD_ADMIN';

INSERT INTO role_permission (role_name, permission_key)
SELECT 'PROPERTY_MANAGER_MEMBER', permission_key FROM role_permission WHERE role_name = 'PROPERTY_BOARD_MEMBER';

-- OWNER: read-only self-service, matches today's ownsParty/ownsUnit checks.
INSERT INTO role_permission (role_name, permission_key) VALUES
    ('PROPERTY_OWNER', 'party:read'),
    ('PROPERTY_OWNER', 'unit:read'),
    ('PROPERTY_OWNER', 'installment:read');

-- Existing app_user_party_role rows only ever used ROLE_PROPERTY_MANAGER
-- (ROLE_PROPERTY_ADMIN was dead/unused) - remap to the closest new
-- equivalent. No production data to preserve; this is a dev/demo-data
-- courtesy remap, not a real data migration.
UPDATE app_user_party_role SET role = 'PROPERTY_MANAGER_ADMIN' WHERE role = 'ROLE_PROPERTY_MANAGER';
