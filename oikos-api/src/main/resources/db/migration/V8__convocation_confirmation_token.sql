-- Lien de confirmation pour les copropriétaires sans compte.
--
-- Une part des lots n'a aucun compte applicatif rattaché : leurs détenteurs
-- reçoivent la convocation mais n'ont, à ce jour, aucun moyen de répondre
-- autrement qu'en appelant le bureau. Le jeton ci-dessous porte un lien
-- personnel, joint à la convocation, qui ouvre une page publique où confirmer
-- sa présence sans s'authentifier.
--
-- Un jeton par convocation, créé à la génération et non à l'envoi : le lien
-- doit figurer sur la lettre elle-même, y compris celle que le syndic imprime
-- pour la poster. Une convocation sans jeton serait une lettre sans lien.
--
-- Pas de colonne d'expiration : le lien cesse d'accepter une réponse à
-- l'ouverture de la séance, ce que le statut de l'AG dit déjà. Une date
-- d'expiration propre dirait moins (elle ignorerait un report de séance) et
-- se désynchroniserait du jour où l'AG bouge.

ALTER TABLE convocation ADD COLUMN confirmation_token character varying(64);

-- Reprise des convocations existantes. Deux UUID v4 concaténés : ~244 bits
-- d'aléa fourni par le SGBD, sans dépendre de pgcrypto (gen_random_bytes n'est
-- pas garanti présent). Les jetons émis ensuite par l'application viennent de
-- SecureRandom via ConvocationTokenGenerator - c'est ce backfill seul qui a
-- besoin d'une source SQL.
UPDATE convocation
SET confirmation_token = replace(gen_random_uuid()::text, '-', '')
                          || replace(gen_random_uuid()::text, '-', '')
WHERE confirmation_token IS NULL;

ALTER TABLE convocation ALTER COLUMN confirmation_token SET NOT NULL;

-- Unique, et c'est structurel : le jeton est la seule chose que présente un
-- visiteur anonyme, donc la seule chose qui désigne la convocation. Deux lots
-- partageant un jeton, c'est une réponse enregistrée sur le mauvais lot.
ALTER TABLE ONLY convocation
    ADD CONSTRAINT uk_convocation_confirmation_token UNIQUE (confirmation_token);
