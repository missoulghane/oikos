-- =========================================================================
-- USER FEATURE: split identity (contact) from login credentials (app_user).
-- app_user no longer carries email/first_name/last_name directly: it references
-- a contact row by id, and gains an optional free-text login. Existing accounts
-- are backfilled into contact reusing their own id (no PK collision: contact is
-- a distinct table), so contact_id = id for every account created before this
-- migration.
-- =========================================================================

ALTER TABLE app_user ADD COLUMN contact_id UUID;
ALTER TABLE app_user ADD COLUMN login VARCHAR(150);

INSERT INTO contact (id, last_name, first_name, email, phone, created_date, last_modified_date, version)
SELECT id, last_name, first_name, email, NULL, created_date, last_modified_date, 0
FROM app_user
ON CONFLICT (id) DO NOTHING;

UPDATE app_user SET contact_id = id;

ALTER TABLE app_user ALTER COLUMN contact_id SET NOT NULL;
ALTER TABLE app_user ADD CONSTRAINT fk_app_user_contact FOREIGN KEY (contact_id) REFERENCES contact (id);
ALTER TABLE app_user ADD CONSTRAINT uk_app_user_login UNIQUE (login);

ALTER TABLE app_user DROP CONSTRAINT uk_app_user_email;
ALTER TABLE app_user DROP COLUMN email;
ALTER TABLE app_user DROP COLUMN first_name;
ALTER TABLE app_user DROP COLUMN last_name;
