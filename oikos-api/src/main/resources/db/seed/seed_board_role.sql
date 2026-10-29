-- Sièges du conseil syndical, sous une forme rejouable - même raison d'être que
-- seed_reply_medium.sql : le profil dev (H2, Flyway désactivé) n'applique jamais
-- V1__baseline.sql et reçoit ses catalogues par spring.sql.init.data-locations.
-- BoardRoleSeedTest vérifie que les deux fichiers déclarent le même catalogue.

INSERT INTO board_role (code, label, position, available_at_signup, active)
SELECT 'VOLUNTEER_MANAGER', 'Syndic bénévole', 1, true, true
WHERE NOT EXISTS (SELECT 1 FROM board_role WHERE code = 'VOLUNTEER_MANAGER');

INSERT INTO board_role (code, label, position, available_at_signup, active)
SELECT 'PRESIDENT', 'Président', 2, true, true
WHERE NOT EXISTS (SELECT 1 FROM board_role WHERE code = 'PRESIDENT');

INSERT INTO board_role (code, label, position, available_at_signup, active)
SELECT 'TREASURER', 'Trésorier', 3, true, true
WHERE NOT EXISTS (SELECT 1 FROM board_role WHERE code = 'TREASURER');

INSERT INTO board_role (code, label, position, available_at_signup, active)
SELECT 'SECRETARY', 'Secrétaire', 4, true, true
WHERE NOT EXISTS (SELECT 1 FROM board_role WHERE code = 'SECRETARY');

INSERT INTO board_role (code, label, position, available_at_signup, active)
SELECT 'MEMBER', 'Membre du conseil syndical', 5, true, true
WHERE NOT EXISTS (SELECT 1 FROM board_role WHERE code = 'MEMBER');

INSERT INTO board_role (code, label, position, available_at_signup, active)
SELECT 'PROPERTY_MANAGER', 'Gestionnaire', 6, false, true
WHERE NOT EXISTS (SELECT 1 FROM board_role WHERE code = 'PROPERTY_MANAGER');
