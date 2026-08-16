-- Convocations : plusieurs canaux d'envoi par convocation, et traçabilité de la
-- confirmation de présence.
--
-- Deux écarts corrigés ici (demande produit du 2026-08-16) :
--
-- 1. Le canal était UNIQUE et s'écrasait. convocation portait un seul triplet
--    (channel, sent_at, delivery_status) que Convocation.recordDelivery()
--    remplaçait à chaque appel : envoyer par email puis constater la remise
--    d'un recommandé effaçait la trace de l'email. Une convocation a désormais
--    N livraisons (convocation_delivery), et son statut d'envoi est DERIVE de
--    ces lignes - jamais stocké, comme le reste des états dérivés du module.
--
-- 2. On savait SI le copropriétaire avait répondu, pas COMMENT. reply_source
--    enregistre la voie empruntée ; elle est déduite de l'appelant côté
--    serveur, jamais envoyée par le client (sinon n'importe qui pourrait
--    écrire "le copropriétaire a confirmé depuis l'app").
--
-- La liste des canaux devient une table de référence et non plus un enum figé
-- avec sa contrainte CHECK : ajouter un canal doit être un INSERT, pas un
-- déploiement. Même bascule que UnitType (enum global) -> UnitTypeDefinition,
-- et même forme que le catalogue `journal` de la comptabilité.


-- Catalogue global (non scopé par property : les canaux d'une copropriété sont
-- ceux du produit).
--
-- `automated` = "l'application sait le faire elle-même". La distinction porte
-- une vraie conséquence : ajouter un canal manuel est gratuit (cette table
-- seule), ajouter un canal automatisé exige en plus un émetteur dans le code -
-- sans quoi la ligne promettrait un envoi qui n'a jamais lieu.
CREATE TABLE convocation_channel (
    code character varying(30) NOT NULL,
    label character varying(100) NOT NULL,
    automated boolean DEFAULT false NOT NULL,
    position integer NOT NULL,
    active boolean DEFAULT true NOT NULL
);

ALTER TABLE ONLY convocation_channel
    ADD CONSTRAINT pk_convocation_channel PRIMARY KEY (code);

-- MOBILE n'est volontairement pas une ligne : la notification in-app (APP) est
-- exactement ce que l'application mobile reçoit, une seconde ligne notifierait
-- deux fois la même chose. Un canal SMS, lui, serait une ligne de plus ici
-- ET un émetteur à écrire.
INSERT INTO convocation_channel (code, label, automated, position) VALUES
    ('EMAIL', 'Email', true, 1),
    ('APP', 'Notification (application)', true, 2),
    ('POSTAL_MAIL', 'Courrier', false, 3),
    ('REGISTERED_MAIL', 'Courrier recommandé avec AR', false, 4),
    ('MANUAL', 'Remise en main propre', false, 5);


-- Une ligne par envoi effectif ou constaté. reference porte le numéro de suivi
-- d'un recommandé (le seul canal qui en produit un) ; recorded_by_user_id dit
-- qui a envoyé ou qui a constaté la remise.
CREATE TABLE convocation_delivery (
    id uuid NOT NULL,
    convocation_id uuid NOT NULL,
    channel_code character varying(30) NOT NULL,
    status character varying(20) NOT NULL,
    sent_at timestamp with time zone,
    reference character varying(100),
    recorded_by_user_id uuid,
    created_date timestamp with time zone NOT NULL,
    last_modified_date timestamp with time zone NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT chk_convocation_delivery_status CHECK (status IN ('SENT', 'FAILED')),
    -- Un envoi abouti porte sa date ; un échec n'en a pas (il n'est jamais parti).
    CONSTRAINT chk_convocation_delivery_sent_at CHECK ((status = 'SENT') = (sent_at IS NOT NULL))
);

ALTER TABLE ONLY convocation_delivery
    ADD CONSTRAINT pk_convocation_delivery PRIMARY KEY (id);

ALTER TABLE ONLY convocation_delivery
    ADD CONSTRAINT fk_convocation_delivery_convocation FOREIGN KEY (convocation_id)
        REFERENCES convocation(id) ON DELETE CASCADE;

ALTER TABLE ONLY convocation_delivery
    ADD CONSTRAINT fk_convocation_delivery_channel FOREIGN KEY (channel_code)
        REFERENCES convocation_channel(code);

-- Pas d'unicité sur (convocation, canal) : renvoyer par le même canal après un
-- échec est une seconde tentative, et les deux méritent d'être lisibles.
CREATE INDEX idx_convocation_delivery_convocation ON convocation_delivery USING btree (convocation_id);


-- Reprise des livraisons déjà enregistrées avant de retirer les colonnes.
-- Les convocations jamais envoyées (channel NULL) n'en produisent aucune :
-- "à envoyer" est désormais l'absence de ligne.
INSERT INTO convocation_delivery (id, convocation_id, channel_code, status, sent_at, recorded_by_user_id,
                                   created_date, last_modified_date, version)
SELECT gen_random_uuid(), c.id, c.channel, c.delivery_status, c.sent_at, NULL,
       c.created_date, c.last_modified_date, 0
FROM convocation c
WHERE c.channel IS NOT NULL AND c.delivery_status <> 'TO_SEND';

ALTER TABLE convocation DROP CONSTRAINT chk_convocation_channel;
ALTER TABLE convocation DROP COLUMN channel;
ALTER TABLE convocation DROP COLUMN sent_at;
ALTER TABLE convocation DROP CONSTRAINT chk_convocation_delivery_status;
ALTER TABLE convocation DROP COLUMN delivery_status;


-- Comment la confirmation a été recueillie. Enum de code (et non catalogue) :
-- contrairement aux canaux, chaque valeur correspond à un chemin de code
-- distinct côté serveur - en ajouter une n'est pas une donnée, c'est une
-- nouvelle façon de répondre.
--   OWNER_APP     le copropriétaire a répondu depuis son espace, authentifié
--   OWNER_LINK    réponse via le lien de confirmation reçu, sans compte
--   SYNDIC_OFFICE réponse faite au bureau de syndic (message, téléphone, oral)
--                 et saisie par le syndic
ALTER TABLE convocation ADD COLUMN reply_source character varying(20);
ALTER TABLE convocation ADD COLUMN replied_by_party_id uuid;
ALTER TABLE convocation ADD COLUMN reply_note character varying(500);

-- Les réponses déjà enregistrées viennent toutes du back-office : c'est le seul
-- chemin qui existait. La reprise précède les contraintes, sinon la seconde
-- refuse les lignes que celle-ci est justement là pour compléter.
UPDATE convocation SET reply_source = 'SYNDIC_OFFICE' WHERE attendance_reply <> 'NO_REPLY';

ALTER TABLE ONLY convocation
    ADD CONSTRAINT chk_convocation_reply_source CHECK (
        reply_source IS NULL OR reply_source IN ('OWNER_APP', 'OWNER_LINK', 'SYNDIC_OFFICE'));

-- Une réponse enregistrée porte forcément sa source ; l'absence de réponse n'en a pas.
ALTER TABLE ONLY convocation
    ADD CONSTRAINT chk_convocation_reply_source_present CHECK (
        (attendance_reply = 'NO_REPLY') = (reply_source IS NULL));

ALTER TABLE ONLY convocation
    ADD CONSTRAINT fk_convocation_replied_by_party FOREIGN KEY (replied_by_party_id) REFERENCES party(id);
