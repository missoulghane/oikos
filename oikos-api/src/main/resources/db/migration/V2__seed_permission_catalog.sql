-- Populates the `permission` reference table (see Permission.java for the
-- closed catalog this mirrors). Missing until now: role_permission.permission_key
-- carries a FK to permission.key (fk_role_permission_permission, V1__baseline.sql),
-- so every role_permission seed insert (db/dev/dev.sql, run via sql.init in both
-- the dev and docker profiles) was silently failing that FK check on Postgres
-- and leaving role_permission empty - continue-on-error swallowed the failure,
-- so every non-admin permission check (@PreAuthorize("@propertyAccess.hasPermission(...)"))
-- denied access in docker/staging while working in dev, where Hibernate's
-- create-drop schema never creates this table or its FK at all.
INSERT INTO permission (key, description) VALUES
    ('property:read', 'Read a property''s details'),
    ('property:create', 'Create a new property'),
    ('property:update', 'Update a property''s details'),
    ('property:board:manage', 'Manage a property''s board members'),
    ('property:member:invite', 'Invite a member onto a property'),
    ('unit:read', 'Read a unit''s details'),
    ('unit:write', 'Create or update a unit'),
    ('unit:ownership:write', 'Create or update a unit''s ownership'),
    ('party:read', 'Read a party''s details'),
    ('party:write', 'Create or update a party'),
    ('party:invite', 'Invite a party onto a property'),
    ('installment:read', 'Read installments and installment calls'),
    ('installment:call:write', 'Create or update an installment call'),
    ('property:accounting:read', 'Read a property''s accounting module'),
    ('property:accounting:write', 'Write to a property''s accounting module'),
    ('user:admin', 'Administer user accounts'),
    ('invitation:manage', 'Manage a property''s invitations and membership requests'),
    ('messaging:broadcast', 'Post to a property''s broadcast announcement channel'),
    ('document:read', 'Read documents'),
    ('document:write', 'Upload or delete documents');
