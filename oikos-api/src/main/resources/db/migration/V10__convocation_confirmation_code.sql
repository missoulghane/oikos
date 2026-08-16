-- Code de confirmation à six caractères, et référence publique de l'AG qui le
-- borne.
--
-- Le jeton de V8 (32 octets) reste la voie normale : c'est lui que porte le QR
-- code, et lui que suit un lien cliqué. Le code court existe pour le cas qu'un
-- jeton ne couvre pas — une lettre papier, sans téléphone pour scanner, où
-- personne ne recopiera 43 caractères à la main.
--
-- Il ne remplace donc rien : il double. Un secret de 34^6 (~1,5 milliard) ne
-- peut pas porter seul ce qu'un secret de 2^256 portait, et c'est ce qui dicte
-- les deux choix ci-dessous.

-- 1. Le code est unique PAR AG, pas globalement. Deviner un code utile suppose
--    d'abord de savoir de quelle assemblée il s'agit, ce qui borne l'espace de
--    recherche aux lots d'une seule copropriété au lieu de toutes.
--
--    Ce qui suppose de pouvoir désigner l'AG sans recopier un UUID : d'où la
--    référence publique, six caractères du même alphabet. Elle ne protège rien
--    (elle est imprimée à côté du code) - elle adresse. Sans elle, « coupler le
--    code à l'AG » voudrait dire écrire 36 caractères sur la lettre pour en
--    économiser 43.
ALTER TABLE general_meeting ADD COLUMN public_reference character varying(6);

UPDATE general_meeting
SET public_reference = substr(replace(gen_random_uuid()::text, '-', ''), 1, 6)
WHERE public_reference IS NULL;

ALTER TABLE general_meeting ALTER COLUMN public_reference SET NOT NULL;

ALTER TABLE ONLY general_meeting
    ADD CONSTRAINT uk_general_meeting_public_reference UNIQUE (public_reference);


-- 2. Le code lui-même. L'alphabet applicatif est [0-9a-z] moins `l` et `o`,
--    confondus avec `1` et `0` sur du papier — c'est la contrainte d'unicité
--    ci-dessous, et non le type, qui porte la garantie qui compte.
--
--    La reprise utilise l'hexadécimal d'un UUID (donc [0-9a-f]), sous-ensemble
--    de l'alphabet applicatif : les codes repris sont valides, simplement moins
--    variés. Ils concernent des convocations déjà émises, dont la lettre est
--    partie sans code de toute façon.
ALTER TABLE convocation ADD COLUMN confirmation_code character varying(6);

UPDATE convocation
SET confirmation_code = substr(replace(gen_random_uuid()::text, '-', ''), 1, 6)
WHERE confirmation_code IS NULL;

ALTER TABLE convocation ALTER COLUMN confirmation_code SET NOT NULL;

-- Unique par AG et non globalement : deux lots de deux copropriétés différentes
-- peuvent porter `w754a1` sans ambiguïté, puisqu'on présente toujours la
-- référence de l'AG avec.
ALTER TABLE ONLY convocation
    ADD CONSTRAINT uk_convocation_meeting_confirmation_code UNIQUE (general_meeting_id, confirmation_code);
