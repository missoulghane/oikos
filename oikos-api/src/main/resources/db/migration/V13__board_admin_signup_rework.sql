-- Refonte de l'inscription du syndic bénévole (retours de recette).
--
-- 1. Le compte est désormais créé dès l'étape 1 du wizard : la capture de
--    l'adresse « avant que le compte n'existe » n'a plus d'objet, la table qui
--    la portait disparaît avec elle.
DROP TABLE public.onboarding_lead;

-- 2. Acceptation des CGV/CGU à l'inscription. Un horodatage plutôt qu'un
--    booléen : c'est la date qui prouve l'acceptation. Nullable : aucun compte
--    existant n'a vu la case, et les comptes créés par un administrateur ou sur
--    invitation ne passent pas par ce formulaire.
ALTER TABLE public.app_user
    ADD COLUMN terms_accepted_at timestamp with time zone;

-- 3. Plus aucun type de lot « OTHERS » n'est créé d'office avec une
--    copropriété. Ceux qui ont été semés jusqu'ici sont retirés tant qu'aucun lot
--    ne les utilise - même règle que la suppression d'un type depuis l'écran
--    Configuration ; leur ligne de prix suit par ON DELETE CASCADE. Un « OTHERS »
--    réellement utilisé par un lot est conservé.
DELETE FROM public.unit_type_definition utd
WHERE utd.name = 'OTHERS'
  AND NOT EXISTS (SELECT 1 FROM public.unit u WHERE u.unit_type_id = utd.id);
