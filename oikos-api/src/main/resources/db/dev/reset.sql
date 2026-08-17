-- Vide les données métier avant de recharger dev.sql.
--
-- Utilisé par le service compose `demo-seed`, jamais par le profil dev local
-- (qui repart d'un schéma H2 recréé à chaque démarrage et n'a donc rien à
-- vider). Il existe pour un seul cas : une recette qu'on veut remettre dans
-- l'état du jeu de démonstration sans détruire son volume PostgreSQL.
--
-- CE QUI SURVIT, et pourquoi ce n'est pas un DROP SCHEMA :
--   * flyway_schema_history — l'effacer ferait rejouer V1 sur un schéma qui
--     existe déjà, donc échouer le déploiement suivant ;
--   * permission, journal, role_permission, convocation_channel, reply_medium —
--     ce sont les données de référence que V1 pose et que le code lit. Les
--     vider laisserait une application qui authentifie et n'autorise rien
--     (role_permission vide = 403 partout), et des listes déroulantes vides.
--
-- Découvert dynamiquement plutôt qu'énuméré : une table ajoutée demain est
-- vidée sans que personne ait à penser à ce fichier. TRUNCATE ... CASCADE
-- traverse les clés étrangères, donc l'ordre n'a pas à être calculé.
DO $$
DECLARE
    r record;
BEGIN
    FOR r IN
        SELECT tablename
        FROM pg_tables
        WHERE schemaname = 'public'
          AND tablename NOT IN ('flyway_schema_history', 'permission', 'journal',
                                'role_permission', 'convocation_channel', 'reply_medium')
    LOOP
        EXECUTE 'TRUNCATE TABLE public.' || quote_ident(r.tablename) || ' RESTART IDENTITY CASCADE';
    END LOOP;
END $$;
