-- =========================================================================
-- COPROPRIETE FEATURE: structure physique d'une copropriete (SFD "Gestion de
-- la Structure des Coproprietes et des Acces").
-- =========================================================================

CREATE TABLE copropriete (
    id                  UUID PRIMARY KEY,
    nom                 VARCHAR(100) NOT NULL,
    adresse             VARCHAR(250) NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE immeuble (
    id                  UUID PRIMARY KEY,
    copropriete_id      UUID NOT NULL,
    nom                 VARCHAR(100) NOT NULL,
    nombre_etages       INTEGER NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_immeuble_copropriete FOREIGN KEY (copropriete_id) REFERENCES copropriete (id)
);

CREATE INDEX idx_immeuble_copropriete_id ON immeuble (copropriete_id);

CREATE TABLE lot (
    id                  UUID PRIMARY KEY,
    immeuble_id         UUID NOT NULL,
    numero_lot          VARCHAR(20) NOT NULL,
    type_lot            VARCHAR(20) NOT NULL,
    tantiemes           NUMERIC(12, 2) NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_lot_immeuble FOREIGN KEY (immeuble_id) REFERENCES immeuble (id)
);

CREATE INDEX idx_lot_immeuble_id ON lot (immeuble_id);
