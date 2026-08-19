-- Le jeton de réinitialisation était conservé tel qu'il partait dans l'email :
-- une base lue - sauvegarde égarée, accès d'exploitation, injection - suffisait
-- à prendre la main sur tout compte ayant une demande en cours. Le jeton de
-- rafraîchissement, lui, ne stockait déjà que son empreinte ; cette table était
-- la dernière à faire exception.

-- Les demandes en vol sont converties plutôt que jetées : sha256() est natif
-- depuis PostgreSQL 11 (le déploiement tourne en 16) et encode(...,'hex') rend
-- l'hexadécimal minuscule que produit HexFormat côté Java. Les liens déjà
-- envoyés restent donc valides jusqu'à leur expiration, une heure au plus.
--
-- convert_to(..., 'UTF8') et non un cast ::bytea : PostgreSQL refuse de caster
-- character varying en bytea, et c'est bien l'encodage UTF-8 que hache le
-- MessageDigest côté Java.
UPDATE public.password_reset_token
SET token = encode(sha256(convert_to(token, 'UTF8')), 'hex');

ALTER TABLE public.password_reset_token RENAME COLUMN token TO token_hash;

-- L'entité déclarait déjà unique = true, sans que la contrainte existe en base :
-- le schéma n'est pas généré par Hibernate hors des tests. Deux empreintes
-- identiques signifieraient deux jetons identiques, ce que le tirage aléatoire
-- de 32 octets ne produit pas - l'index protège de l'anomalie, il ne corrige
-- rien d'existant.
CREATE UNIQUE INDEX password_reset_token_token_hash_key
    ON public.password_reset_token (token_hash);
