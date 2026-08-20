-- La copropriété ne portait qu'une adresse en un seul champ, alors que le
-- parcours d'inscription demande bien deux choses : l'adresse et la ville. Le
-- wizard les recollait donc en « adresse, ville » avant l'envoi, et la fiche
-- copropriété ne pouvait plus les afficher ni les corriger séparément.

ALTER TABLE public.property ADD COLUMN city character varying(100);

-- Volontairement aucune reprise des données existantes : les adresses déjà
-- saisies sont de la saisie libre (« 12 rue des Écoles, Bât. B, Casablanca »),
-- et découper sur la dernière virgule mettrait « Bât. B » en ville une fois sur
-- deux. Les copropriétés antérieures gardent leur adresse telle quelle, ville
-- vide, jusqu'à ce qu'un syndic la renseigne depuis sa fiche.
