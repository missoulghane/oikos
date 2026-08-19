-- Le nom d'une personne s'écrit en un seul champ partout dans ce schéma
-- (users.full_name, party.full_name) - onboarding_lead était la seule table à
-- le couper en deux, héritage du formulaire de l'étape 1 du wizard qui
-- demandait « Prénom » et « Nom ». Ce formulaire demande désormais le nom
-- complet, comme tous les autres écrans de saisie du produit.

ALTER TABLE public.onboarding_lead ADD COLUMN full_name character varying(200);

-- concat_ws ignore les NULL : les deux colonnes étaient nullables, une seule
-- renseignée donne le nom sans espace parasite, aucune des deux donne '' que
-- NULLIF ramène à NULL (full_name reste nullable, comme les colonnes remplacées).
UPDATE public.onboarding_lead
SET full_name = NULLIF(btrim(concat_ws(' ', first_name, last_name)), '');

ALTER TABLE public.onboarding_lead DROP COLUMN first_name;
ALTER TABLE public.onboarding_lead DROP COLUMN last_name;
