-- Module "meeting" (assemblée générale) - schéma complet.
-- Cadrage fonctionnel et arbitrages : docs/adr/0002-assemblee-generale-cadrage.md
-- Correspondance FR -> EN : docs/NOMENCLATURE.md, section "Module assemblée générale".
--
-- DDL uniquement, plus les trois lignes de catalogue `permission`. Les bundles
-- role -> permission sont dans V5, à part : le profil dev (H2, Flyway désactivé,
-- ddl-auto create-drop) charge les bundles via spring.sql.init mais ne connaît
-- ni ce fichier ni la table `permission` (aucune entité JPA ne la déclare) -
-- mélanger les deux rendrait V5 inchargeable en dev. Même séparation que
-- V1 (DDL + catalogue) / V2 (bundles).


-- Seuil de quorum, réglable par copropriété ET par nature d'AG (ordinaire vs
-- extraordinaire n'exigent pas le même quorum). Absence de ligne = aucun quorum
-- exigé : on n'invente pas un seuil légal par défaut, le syndic le paramètre.
-- La valeur est recopiée sur general_meeting.quorum_percentage à la création de
-- l'AG - modifier le paramétrage ne doit pas réécrire une AG passée.
CREATE TABLE meeting_quorum_setting (
    id uuid NOT NULL,
    property_id uuid NOT NULL,
    meeting_type character varying(20) NOT NULL,
    quorum_percentage numeric(5,2) NOT NULL,
    created_date timestamp with time zone NOT NULL,
    last_modified_date timestamp with time zone NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT chk_meeting_quorum_setting_type CHECK (meeting_type IN ('ORDINARY', 'EXTRAORDINARY')),
    CONSTRAINT chk_meeting_quorum_setting_percentage CHECK (quorum_percentage >= 0 AND quorum_percentage <= 100)
);

ALTER TABLE ONLY meeting_quorum_setting
    ADD CONSTRAINT pk_meeting_quorum_setting PRIMARY KEY (id);

ALTER TABLE ONLY meeting_quorum_setting
    ADD CONSTRAINT uk_meeting_quorum_setting_property_type UNIQUE (property_id, meeting_type);

ALTER TABLE ONLY meeting_quorum_setting
    ADD CONSTRAINT fk_meeting_quorum_setting_property FOREIGN KEY (property_id)
        REFERENCES property(id) ON DELETE CASCADE;


-- L'assemblée générale elle-même. scheduled_at et le lieu sont nuls tant que
-- l'AG est un brouillon, et obligatoires dès qu'elle quitte DRAFT (les deux
-- CHECK ci-dessous) : le cycle de vie est porté par l'agrégat, ces contraintes
-- ne sont qu'un filet, pas la règle métier.
--
-- quorum_percentage et voting_weight_mode sont des SNAPSHOTS, figés à la
-- création : le premier vient de meeting_quorum_setting, le second de
-- property.dues_calculation_mode (FLAT_RATE -> PER_UNIT, SHARES -> SHARES). Une
-- bascule de configuration de la copropriété ne doit pas changer après coup la
-- façon dont une AG déjà tenue a été dépouillée - même principe que le gel du
-- prix d'un appel de fonds déjà émis (RG002).
--
-- opened_without_quorum trace une ouverture de séance forcée alors que le
-- quorum n'était pas atteint. C'est un acte juridique délibéré du syndic, il
-- doit être visible dans le PV, donc persisté et pas déduit.
CREATE TABLE general_meeting (
    id uuid NOT NULL,
    property_id uuid NOT NULL,
    meeting_type character varying(20) NOT NULL,
    status character varying(20) DEFAULT 'DRAFT' NOT NULL,
    title character varying(200) NOT NULL,
    scheduled_at timestamp with time zone,
    venue_type character varying(20),
    venue_address character varying(250),
    venue_link character varying(500),
    quorum_percentage numeric(5,2) NOT NULL,
    voting_weight_mode character varying(20) NOT NULL,
    opened_without_quorum boolean DEFAULT false NOT NULL,
    created_date timestamp with time zone NOT NULL,
    last_modified_date timestamp with time zone NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT chk_general_meeting_type CHECK (meeting_type IN ('ORDINARY', 'EXTRAORDINARY')),
    CONSTRAINT chk_general_meeting_status CHECK (status IN ('DRAFT', 'SCHEDULED', 'CONVENED', 'IN_PROGRESS', 'CLOSED', 'MINUTES_PUBLISHED')),
    CONSTRAINT chk_general_meeting_weight_mode CHECK (voting_weight_mode IN ('PER_UNIT', 'SHARES')),
    CONSTRAINT chk_general_meeting_quorum_percentage CHECK (quorum_percentage >= 0 AND quorum_percentage <= 100),
    CONSTRAINT chk_general_meeting_scheduled CHECK (status = 'DRAFT' OR (scheduled_at IS NOT NULL AND venue_type IS NOT NULL)),
    CONSTRAINT chk_general_meeting_venue CHECK (
        venue_type IS NULL
        OR (venue_type = 'PHYSICAL' AND venue_address IS NOT NULL)
        OR (venue_type = 'VIDEOCONFERENCE' AND venue_link IS NOT NULL)
        OR (venue_type = 'HYBRID' AND venue_address IS NOT NULL AND venue_link IS NOT NULL)
    )
);

ALTER TABLE ONLY general_meeting
    ADD CONSTRAINT pk_general_meeting PRIMARY KEY (id);

ALTER TABLE ONLY general_meeting
    ADD CONSTRAINT fk_general_meeting_property FOREIGN KEY (property_id)
        REFERENCES property(id) ON DELETE CASCADE;

CREATE INDEX idx_general_meeting_property ON general_meeting USING btree (property_id, scheduled_at);


-- Un point de l'ordre du jour. majority_rule est obligatoire et choisie point
-- par point : c'est elle qui décide de l'adoption, elle ne peut pas être un
-- réglage global de l'AG.
--
-- Aucune colonne de résultat : le dépouillement (VoteTally/VoteOutcome) est
-- recalculé à la lecture depuis `vote`, jamais stocké - convention constante du
-- codebase (InstallmentStatus, OwnershipStatus). Il n'est figé qu'une fois,
-- dans le contenu du procès-verbal. vote_session_status, lui, est un vrai état
-- de séance : non dérivable, donc persisté.
CREATE TABLE agenda_item (
    id uuid NOT NULL,
    general_meeting_id uuid NOT NULL,
    label character varying(200) NOT NULL,
    description text,
    position integer NOT NULL,
    majority_rule character varying(20) NOT NULL,
    vote_session_status character varying(20) DEFAULT 'NOT_OPENED' NOT NULL,
    created_date timestamp with time zone NOT NULL,
    last_modified_date timestamp with time zone NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT chk_agenda_item_majority_rule CHECK (majority_rule IN ('SIMPLE', 'ABSOLUTE', 'UNANIMITY')),
    CONSTRAINT chk_agenda_item_vote_session CHECK (vote_session_status IN ('NOT_OPENED', 'OPEN', 'CLOSED')),
    CONSTRAINT chk_agenda_item_position CHECK (position >= 0)
);

ALTER TABLE ONLY agenda_item
    ADD CONSTRAINT pk_agenda_item PRIMARY KEY (id);

ALTER TABLE ONLY agenda_item
    ADD CONSTRAINT fk_agenda_item_general_meeting FOREIGN KEY (general_meeting_id)
        REFERENCES general_meeting(id) ON DELETE CASCADE;

-- DEFERRABLE INITIALLY DEFERRED : réordonner l'ordre du jour permute des
-- positions, et une permutation passe forcément par un état transitoire en
-- doublon. Sans report en fin de transaction, tout réordonnancement échouerait.
ALTER TABLE ONLY agenda_item
    ADD CONSTRAINT uk_agenda_item_position UNIQUE (general_meeting_id, position)
        DEFERRABLE INITIALLY DEFERRED;


-- Convocation : objet unique couvrant envoi + confirmation + émargement, comme
-- dans la SFD.
--
-- Le sujet est le LOT, pas le copropriétaire (ADR 0002 §2) : un lot en
-- indivision reçoit une seule convocation et n'a qu'une voix, un copropriétaire
-- de trois lots en reçoit trois. Les destinataires effectifs (les propriétaires
-- courants du lot) ne sont pas stockés ici : ils sont résolus à l'envoi, comme
-- l'appartenance d'un canal BROADCAST dans `messaging` - une vente de lot entre
-- la convocation et la séance ne laisse alors rien à migrer.
--
-- Une convocation est générée pour TOUS les lots de la copropriété, y compris
-- ceux sans aucun unit_ownership : ils comptent dans le total des voix (donc
-- dans le quorum et dans la majorité absolue) mais ne peuvent ni répondre ni
-- émarger.
--
-- voting_weight est un snapshot (1 en mode PER_UNIT, les tantièmes du lot en
-- mode SHARES) : sans lui, une vente ou une correction de tantièmes rendrait un
-- résultat d'AG passée irreproductible.
--
-- attendance_reply vaut NO_REPLY par défaut plutôt que NULL : "n'a pas répondu"
-- est un état du parcours, pas une donnée manquante.
--
-- checked_in_party_id est la seule trace nominative de l'émargement (qui
-- représentait le lot). Ce n'est pas un mandat : les procurations sont hors
-- périmètre v1.
CREATE TABLE convocation (
    id uuid NOT NULL,
    general_meeting_id uuid NOT NULL,
    unit_id uuid NOT NULL,
    voting_weight numeric(12,2) NOT NULL,
    channel character varying(20),
    sent_at timestamp with time zone,
    delivery_status character varying(20) DEFAULT 'TO_SEND' NOT NULL,
    attendance_reply character varying(20) DEFAULT 'NO_REPLY' NOT NULL,
    replied_at timestamp with time zone,
    checked_in boolean DEFAULT false NOT NULL,
    attendance_mode character varying(20),
    checked_in_party_id uuid,
    checked_in_at timestamp with time zone,
    created_date timestamp with time zone NOT NULL,
    last_modified_date timestamp with time zone NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT chk_convocation_channel CHECK (channel IS NULL OR channel IN ('EMAIL', 'APP', 'POSTAL_MAIL', 'REGISTERED_MAIL')),
    CONSTRAINT chk_convocation_delivery_status CHECK (delivery_status IN ('TO_SEND', 'SENT', 'FAILED')),
    CONSTRAINT chk_convocation_attendance_reply CHECK (attendance_reply IN ('ATTENDING', 'NOT_ATTENDING', 'NO_REPLY')),
    CONSTRAINT chk_convocation_attendance_mode CHECK (attendance_mode IS NULL OR attendance_mode IN ('ON_SITE', 'REMOTE')),
    CONSTRAINT chk_convocation_voting_weight CHECK (voting_weight >= 0),
    CONSTRAINT chk_convocation_check_in CHECK (checked_in = false OR (attendance_mode IS NOT NULL AND checked_in_at IS NOT NULL))
);

ALTER TABLE ONLY convocation
    ADD CONSTRAINT pk_convocation PRIMARY KEY (id);

ALTER TABLE ONLY convocation
    ADD CONSTRAINT uk_convocation_meeting_unit UNIQUE (general_meeting_id, unit_id);

ALTER TABLE ONLY convocation
    ADD CONSTRAINT fk_convocation_general_meeting FOREIGN KEY (general_meeting_id)
        REFERENCES general_meeting(id) ON DELETE CASCADE;

ALTER TABLE ONLY convocation
    ADD CONSTRAINT fk_convocation_unit FOREIGN KEY (unit_id) REFERENCES unit(id);

ALTER TABLE ONLY convocation
    ADD CONSTRAINT fk_convocation_checked_in_party FOREIGN KEY (checked_in_party_id) REFERENCES party(id);

CREATE INDEX idx_convocation_unit ON convocation USING btree (unit_id);


-- Un vote par lot et par point (le lot vote, ADR 0002 §2). cast_by_user_id ne
-- trace que qui a saisi le vote (le copropriétaire lui-même, ou le syndic lors
-- d'un vote à main levée saisi en masse) - jamais qui "détient" la voix, qui est
-- toujours le lot. Nullable : une saisie en masse peut précéder tout compte
-- applicatif rattaché.
--
-- Immuable comme Movement/Message : un vote se corrige en le remplaçant sous
-- l'unicité ci-dessous tant que la session est OPEN, jamais après clôture.
CREATE TABLE vote (
    id uuid NOT NULL,
    agenda_item_id uuid NOT NULL,
    unit_id uuid NOT NULL,
    choice character varying(20) NOT NULL,
    cast_at timestamp with time zone NOT NULL,
    cast_by_user_id uuid,
    created_date timestamp with time zone NOT NULL,
    last_modified_date timestamp with time zone NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT chk_vote_choice CHECK (choice IN ('FOR', 'AGAINST', 'ABSTENTION'))
);

ALTER TABLE ONLY vote
    ADD CONSTRAINT pk_vote PRIMARY KEY (id);

ALTER TABLE ONLY vote
    ADD CONSTRAINT uk_vote_agenda_item_unit UNIQUE (agenda_item_id, unit_id);

ALTER TABLE ONLY vote
    ADD CONSTRAINT fk_vote_agenda_item FOREIGN KEY (agenda_item_id)
        REFERENCES agenda_item(id) ON DELETE CASCADE;

ALTER TABLE ONLY vote
    ADD CONSTRAINT fk_vote_unit FOREIGN KEY (unit_id) REFERENCES unit(id);

ALTER TABLE ONLY vote
    ADD CONSTRAINT fk_vote_cast_by_user FOREIGN KEY (cast_by_user_id) REFERENCES app_user(id);


-- Procès-verbal, 1-1 avec l'AG (d'où l'unicité sur general_meeting_id).
-- `content` porte le PV rédigé (présents, résultats, décisions) ; c'est là que
-- le dépouillement, calculé partout ailleurs, est figé une fois pour toutes. Le
-- PDF final n'est pas une colonne : il est stocké par le module `document`
-- (DocumentOwnerType.MEETING_MINUTES), comme le reçu de paiement.
CREATE TABLE meeting_minutes (
    id uuid NOT NULL,
    general_meeting_id uuid NOT NULL,
    content text NOT NULL,
    status character varying(20) DEFAULT 'DRAFT' NOT NULL,
    published_at timestamp with time zone,
    created_date timestamp with time zone NOT NULL,
    last_modified_date timestamp with time zone NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT chk_meeting_minutes_status CHECK (status IN ('DRAFT', 'UNDER_REVIEW', 'PUBLISHED')),
    CONSTRAINT chk_meeting_minutes_published CHECK (status <> 'PUBLISHED' OR published_at IS NOT NULL)
);

ALTER TABLE ONLY meeting_minutes
    ADD CONSTRAINT pk_meeting_minutes PRIMARY KEY (id);

ALTER TABLE ONLY meeting_minutes
    ADD CONSTRAINT uk_meeting_minutes_general_meeting UNIQUE (general_meeting_id);

ALTER TABLE ONLY meeting_minutes
    ADD CONSTRAINT fk_meeting_minutes_general_meeting FOREIGN KEY (general_meeting_id)
        REFERENCES general_meeting(id) ON DELETE CASCADE;


-- Catalogue RBAC : les trois clés que role_permission (V5) référence par FK.
-- La publication du PV est séparée de meeting:manage parce qu'elle est
-- irréversible et diffusée à tous les copropriétaires - un rôle peut légitimement
-- préparer une AG sans avoir le droit de publier son procès-verbal.
INSERT INTO permission (key, description) VALUES
    ('meeting:read', 'Read a property''s general meetings'),
    ('meeting:manage', 'Create and run a property''s general meetings'),
    ('meeting:minutes:publish', 'Validate and publish a general meeting''s minutes');
