-- Catalogue des moyens de réception d'une réponse, sous une forme rejouable.
--
-- Même raison d'être que V7 pour les canaux : le profil dev tourne sur H2 avec
-- Flyway désactivé et le schéma généré par Hibernate. Il n'applique jamais V12 -
-- il ne peut pas, V12 supprime et recrée des contraintes qui n'existent pas dans
-- un schéma fraîchement généré. Il charge en revanche les migrations de données
-- comme de simples scripts (spring.sql.init.data-locations), et c'est par là que
-- le catalogue doit lui parvenir. Sans cette liste, la liste déroulante « Reçue
-- par » serait vide en dev et pleine en prod.
--
-- En prod, V12 a déjà posé ces lignes : chaque INSERT y est donc un no-op. La
-- forme INSERT ... SELECT ... WHERE NOT EXISTS est celle que H2 accepte aussi
-- (pas de ON CONFLICT), et elle rend le script rejouable à volonté.
--
-- ReplyMediumSeedTest vérifie que les deux fichiers déclarent le même catalogue :
-- la duplication est assumée, sa dérive ne l'est pas.

INSERT INTO reply_medium (code, label, position, active)
SELECT 'TELEPHONE', 'Téléphone', 1, true
WHERE NOT EXISTS (SELECT 1 FROM reply_medium WHERE code = 'TELEPHONE');

INSERT INTO reply_medium (code, label, position, active)
SELECT 'COURRIER', 'Courrier', 2, true
WHERE NOT EXISTS (SELECT 1 FROM reply_medium WHERE code = 'COURRIER');

INSERT INTO reply_medium (code, label, position, active)
SELECT 'EMAIL', 'Email', 3, true
WHERE NOT EXISTS (SELECT 1 FROM reply_medium WHERE code = 'EMAIL');

INSERT INTO reply_medium (code, label, position, active)
SELECT 'GUICHET', 'Au bureau', 4, true
WHERE NOT EXISTS (SELECT 1 FROM reply_medium WHERE code = 'GUICHET');
