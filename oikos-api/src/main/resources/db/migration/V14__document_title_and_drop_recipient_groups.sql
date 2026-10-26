-- Retours de recette : documents et messagerie.
--
-- 1. Titre d'un document : ce que l'on lit et recherche dans la liste, saisi au
--    dépôt ou, à défaut, déduit du nom du fichier (extension retirée - même
--    règle que Document.defaultTitle). Les documents existants reçoivent le nom
--    de leur fichier, borné à la longueur du titre.
ALTER TABLE public.document ADD COLUMN title character varying(200);

UPDATE public.document
SET title = left(
        CASE
            WHEN position('.' IN file_name) > 1
                THEN btrim(regexp_replace(file_name, '\.[^.]*$', ''))
            ELSE btrim(file_name)
        END,
        200);

-- Un nom réduit à rien après la coupe (« .pdf ») garde son nom entier.
UPDATE public.document SET title = left(file_name, 200) WHERE title IS NULL OR title = '';

ALTER TABLE public.document ALTER COLUMN title SET NOT NULL;

-- 2. Suppression des groupes de diffusion (carnet d'adresses de la messagerie).
--    Ils ne servaient qu'à composer : un envoi crée une conversation ordinaire
--    avec les membres comme participants, sans garder de lien vers le groupe
--    (voir V8). Aucune conversation ni aucun message n'est donc perdu ; seules
--    les listes de destinataires enregistrées disparaissent.
--    IF EXISTS : une base peut ne jamais avoir reçu V8 (constaté sur une base
--    locale passée de V7 à V9) - il n'y a alors rien à supprimer.
DROP TABLE IF EXISTS public.recipient_group_member;
DROP TABLE IF EXISTS public.recipient_group;
