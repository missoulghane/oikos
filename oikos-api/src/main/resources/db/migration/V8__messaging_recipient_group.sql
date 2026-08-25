-- Carnet d'adresses de la messagerie : une liste de destinataires nommée, par
-- copropriété (« Habitants du bâtiment 1 »). Entretenue par le bureau, elle ne
-- sert qu'à composer - un envoi crée une conversation ordinaire avec les
-- membres du groupe comme participants, et n'en garde aucun lien vers le
-- groupe (voir RecipientGroup).
CREATE TABLE recipient_group (
    id uuid NOT NULL,
    property_id uuid NOT NULL,
    name character varying(120) NOT NULL,
    created_by uuid NOT NULL,
    created_date timestamp with time zone NOT NULL,
    last_modified_date timestamp with time zone NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT pk_recipient_group PRIMARY KEY (id)
);

-- Un nom par copropriété : deux « Bâtiment 1 » dans la même liste ne se
-- distinguent pas au moment de composer.
CREATE UNIQUE INDEX uk_recipient_group_property_name ON recipient_group (property_id, lower(name));

CREATE INDEX idx_recipient_group_property ON recipient_group (property_id);

-- Même patron que conversation_participant : une ligne par (groupe, membre).
CREATE TABLE recipient_group_member (
    recipient_group_id uuid NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT pk_recipient_group_member PRIMARY KEY (recipient_group_id, user_id),
    CONSTRAINT fk_recipient_group_member_group FOREIGN KEY (recipient_group_id)
        REFERENCES recipient_group (id) ON DELETE CASCADE
);
