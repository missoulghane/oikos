-- Bundles role -> permission du module `meeting` (assemblée générale).
-- Les trois clés référencées sont cataloguées par V4.
--
-- Fichier séparé de V4 pour la même raison que V2 l'est de V1 : le profil dev
-- (H2, Flyway désactivé) charge les bundles directement via spring.sql.init, et
-- ne connaît ni la table `permission` (aucune entité JPA ne la déclare, donc
-- ddl-auto ne la crée pas) ni le DDL PostgreSQL de V4. Ce fichier doit donc
-- rester chargeable seul, sur H2 comme sur PostgreSQL - d'où l'absence de
-- ON CONFLICT (clause PostgreSQL uniquement) au profit d'un NOT EXISTS, exactement
-- comme V2. Toute nouvelle permission ajoutée ici doit l'être aussi dans
-- application-dev.yml (data-locations) si un nouveau fichier de seed apparaît.
--
-- Mirroring bureau/gérant identique à celui des permissions de V2 :
-- PROPERTY_OWNER ne reçoit que la lecture - un copropriétaire consulte ses AG,
-- ses convocations et les PV publiés, il n'en organise aucune.
INSERT INTO role_permission (role_name, permission_key)
SELECT candidate.role_name, candidate.permission_key
FROM (VALUES
    ('ROLE_ADMIN', 'meeting:read'),
    ('ROLE_ADMIN', 'meeting:manage'),
    ('ROLE_ADMIN', 'meeting:minutes:publish'),
    ('PROPERTY_BOARD_ADMIN', 'meeting:read'),
    ('PROPERTY_BOARD_ADMIN', 'meeting:manage'),
    ('PROPERTY_BOARD_ADMIN', 'meeting:minutes:publish'),
    ('PROPERTY_BOARD_MEMBER', 'meeting:read'),
    ('PROPERTY_BOARD_MEMBER', 'meeting:manage'),
    ('PROPERTY_BOARD_MEMBER', 'meeting:minutes:publish'),
    ('PROPERTY_MANAGER_ADMIN', 'meeting:read'),
    ('PROPERTY_MANAGER_ADMIN', 'meeting:manage'),
    ('PROPERTY_MANAGER_ADMIN', 'meeting:minutes:publish'),
    ('PROPERTY_MANAGER_MEMBER', 'meeting:read'),
    ('PROPERTY_MANAGER_MEMBER', 'meeting:manage'),
    ('PROPERTY_MANAGER_MEMBER', 'meeting:minutes:publish'),
    ('PROPERTY_OWNER', 'meeting:read')
) AS candidate(role_name, permission_key)
WHERE NOT EXISTS (
    SELECT 1 FROM role_permission existing
    WHERE existing.role_name = candidate.role_name
      AND existing.permission_key = candidate.permission_key
);
