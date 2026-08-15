-- role_permission: the role -> permission bundles GetUserAccessService resolves
-- every authorization from (via RolePermissionRepository). Without these rows
-- permissionsByProperty is empty for every account, so every @PreAuthorize that
-- checks a Permission denies - a database provisioned by Flyway alone would come
-- up authenticating fine and authorizing nothing.
--
-- Third table hit by the same 2026-08-13 `pg_dump --schema-only` squash that
-- already cost the `permission` catalog and the `journal` reference table (both
-- folded back into V1 - see its header).
--
-- THIS FILE IS THE SINGLE SOURCE OF THE BUNDLES. db/dev/dev.sql used to carry
-- its own copy "kept in sync manually" - that manual sync is exactly what failed
-- three times, so dev.sql no longer declares them: the dev profile (H2, Flyway
-- disabled) loads this very file through spring.sql.init instead, and
-- RolePermissionSeedTest fails the build if the catalog, the Permission enum and
-- these bundles ever drift apart.
--
-- Idempotent through NOT EXISTS rather than ON CONFLICT: environments that
-- predate the squash still hold these rows and this must be a no-op there, and
-- the statement has to run on H2 (dev profile) as well as PostgreSQL, which
-- rules out the PostgreSQL-only ON CONFLICT clause.
-- permission_key FKs to permission(key), seeded earlier in V1.

INSERT INTO role_permission (role_name, permission_key)
SELECT candidate.role_name, candidate.permission_key
FROM (VALUES
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
    ('ROLE_ADMIN', 'messaging:broadcast'),
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
    ('PROPERTY_BOARD_ADMIN', 'messaging:broadcast'),
    ('PROPERTY_BOARD_MEMBER', 'property:read'),
    ('PROPERTY_BOARD_MEMBER', 'property:create'),
    ('PROPERTY_BOARD_MEMBER', 'property:update'),
    ('PROPERTY_BOARD_MEMBER', 'property:board:manage'),
    ('PROPERTY_BOARD_MEMBER', 'property:member:invite'),
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
    ('PROPERTY_BOARD_MEMBER', 'invitation:manage'),
    ('PROPERTY_BOARD_MEMBER', 'document:read'),
    ('PROPERTY_BOARD_MEMBER', 'document:write'),
    ('PROPERTY_BOARD_MEMBER', 'messaging:broadcast'),
    ('PROPERTY_MANAGER_ADMIN', 'property:read'),
    ('PROPERTY_MANAGER_ADMIN', 'property:create'),
    ('PROPERTY_MANAGER_ADMIN', 'property:update'),
    ('PROPERTY_MANAGER_ADMIN', 'property:board:manage'),
    ('PROPERTY_MANAGER_ADMIN', 'property:member:invite'),
    ('PROPERTY_MANAGER_ADMIN', 'unit:read'),
    ('PROPERTY_MANAGER_ADMIN', 'unit:write'),
    ('PROPERTY_MANAGER_ADMIN', 'unit:ownership:write'),
    ('PROPERTY_MANAGER_ADMIN', 'party:read'),
    ('PROPERTY_MANAGER_ADMIN', 'party:write'),
    ('PROPERTY_MANAGER_ADMIN', 'party:invite'),
    ('PROPERTY_MANAGER_ADMIN', 'installment:read'),
    ('PROPERTY_MANAGER_ADMIN', 'installment:call:write'),
    ('PROPERTY_MANAGER_ADMIN', 'property:accounting:read'),
    ('PROPERTY_MANAGER_ADMIN', 'property:accounting:write'),
    ('PROPERTY_MANAGER_ADMIN', 'invitation:manage'),
    ('PROPERTY_MANAGER_ADMIN', 'document:read'),
    ('PROPERTY_MANAGER_ADMIN', 'document:write'),
    ('PROPERTY_MANAGER_ADMIN', 'messaging:broadcast'),
    ('PROPERTY_MANAGER_MEMBER', 'property:read'),
    ('PROPERTY_MANAGER_MEMBER', 'property:create'),
    ('PROPERTY_MANAGER_MEMBER', 'property:update'),
    ('PROPERTY_MANAGER_MEMBER', 'property:board:manage'),
    ('PROPERTY_MANAGER_MEMBER', 'property:member:invite'),
    ('PROPERTY_MANAGER_MEMBER', 'unit:read'),
    ('PROPERTY_MANAGER_MEMBER', 'unit:write'),
    ('PROPERTY_MANAGER_MEMBER', 'unit:ownership:write'),
    ('PROPERTY_MANAGER_MEMBER', 'party:read'),
    ('PROPERTY_MANAGER_MEMBER', 'party:write'),
    ('PROPERTY_MANAGER_MEMBER', 'party:invite'),
    ('PROPERTY_MANAGER_MEMBER', 'installment:read'),
    ('PROPERTY_MANAGER_MEMBER', 'installment:call:write'),
    ('PROPERTY_MANAGER_MEMBER', 'property:accounting:read'),
    ('PROPERTY_MANAGER_MEMBER', 'property:accounting:write'),
    ('PROPERTY_MANAGER_MEMBER', 'invitation:manage'),
    ('PROPERTY_MANAGER_MEMBER', 'document:read'),
    ('PROPERTY_MANAGER_MEMBER', 'document:write'),
    ('PROPERTY_MANAGER_MEMBER', 'messaging:broadcast'),
    ('PROPERTY_OWNER', 'party:read'),
    ('PROPERTY_OWNER', 'unit:read'),
    ('PROPERTY_OWNER', 'installment:read'),
    ('PROPERTY_OWNER', 'document:read')
) AS candidate(role_name, permission_key)
WHERE NOT EXISTS (
    SELECT 1 FROM role_permission existing
    WHERE existing.role_name = candidate.role_name
      AND existing.permission_key = candidate.permission_key
);
