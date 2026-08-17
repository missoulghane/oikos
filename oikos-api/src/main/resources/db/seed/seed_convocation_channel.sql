-- Catalogue des canaux de convocation, sous une forme rejouable.
--
-- Pourquoi une seconde fois, alors que V6 insère déjà ces lignes : le profil dev
-- tourne sur H2 avec Flyway désactivé et le schéma généré par Hibernate. Il
-- n'applique jamais V6 - il ne peut pas, V6 supprime des colonnes qui n'existent
-- pas dans un schéma fraîchement généré. Il charge en revanche les migrations de
-- données comme de simples scripts (spring.sql.init.data-locations), et c'est
-- par là que le catalogue doit lui parvenir. Sans cette liste, l'écran d'envoi
-- des convocations n'offrirait aucun canal en dev tout en fonctionnant en prod -
-- exactement l'incident que V5 et RolePermissionSeedTest ont déjà traité pour
-- les permissions.
--
-- En prod, V6 a déjà posé ces lignes : chaque INSERT y est donc un no-op. La
-- forme INSERT ... SELECT ... WHERE NOT EXISTS est celle que H2 accepte aussi
-- (pas de ON CONFLICT), et elle rend le script rejouable à volonté.
--
-- ConvocationChannelSeedTest vérifie que les deux fichiers déclarent le même
-- catalogue : la duplication est assumée, sa dérive ne l'est pas.

INSERT INTO convocation_channel (code, label, automated, position, active)
SELECT 'EMAIL', 'Email', true, 1, true
WHERE NOT EXISTS (SELECT 1 FROM convocation_channel WHERE code = 'EMAIL');

INSERT INTO convocation_channel (code, label, automated, position, active)
SELECT 'APP', 'Messagerie interne', true, 2, true
WHERE NOT EXISTS (SELECT 1 FROM convocation_channel WHERE code = 'APP');

INSERT INTO convocation_channel (code, label, automated, position, active)
SELECT 'POSTAL_MAIL', 'Courrier', false, 3, true
WHERE NOT EXISTS (SELECT 1 FROM convocation_channel WHERE code = 'POSTAL_MAIL');

INSERT INTO convocation_channel (code, label, automated, position, active)
SELECT 'REGISTERED_MAIL', 'Courrier recommandé avec AR', false, 4, true
WHERE NOT EXISTS (SELECT 1 FROM convocation_channel WHERE code = 'REGISTERED_MAIL');

INSERT INTO convocation_channel (code, label, automated, position, active)
SELECT 'MANUAL', 'Remise en main propre', false, 5, true
WHERE NOT EXISTS (SELECT 1 FROM convocation_channel WHERE code = 'MANUAL');
