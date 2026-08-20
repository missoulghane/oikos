-- Un contact pouvait exister sans téléphone, jamais sans email. Or un membre du
-- conseil syndical ou un copropriétaire n'a parfois qu'un mobile, et le
-- formulaire annonçait d'ailleurs « Email (optionnel) » : l'API répondait 500,
-- l'agrégat Party refusant un email nul.

ALTER TABLE public.party ALTER COLUMN email DROP NOT NULL;

-- uk_party_property_email (property_id, email) reste en place : PostgreSQL
-- considère deux NULL comme distincts, plusieurs contacts sans adresse
-- cohabitent donc dans la même copropriété, exactement comme le fait déjà
-- uk_party_property_phone pour les contacts sans téléphone.
