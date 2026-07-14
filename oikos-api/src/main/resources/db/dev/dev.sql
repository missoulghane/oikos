-- =========================================================================
-- dev-only
-- =========================================================================
INSERT INTO contact (id, last_name, first_name, email, phone, created_date, last_modified_date, version)
VALUES ( '402888b2-2370-4c5e-aba6-985da776bb17', 'Root', 'OIKOS', 'oikos@architek.com', NULL, now(), now(), 0 );

INSERT INTO app_user (id, contact_id, password_hash, login, verified, enabled, created_date, last_modified_date, version)
VALUES ( '402888b2-2370-4c5e-aba6-985da776bb17', '402888b2-2370-4c5e-aba6-985da776bb17', '$2y$10$E1hvM5POd0hg3VZ1wpPjxutufECIqAY2bbjtQb8tfWL60IYI7q7RC', NULL, TRUE, TRUE, now(), now(), 0 );

INSERT INTO user_role (user_id, role)
VALUES ('402888b2-2370-4c5e-aba6-985da776bb17', 'ROLE_MASTER'),
       ('402888b2-2370-4c5e-aba6-985da776bb17', 'ROLE_ADMIN');
