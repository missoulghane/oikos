-- =========================================================================
-- COPROPRIETE FEATURE: pivots copropretaire (propriete_lot) et organe de
-- gestion (membre_syndic), tous deux rattaches a un contact (SFD "Gestion de
-- la Structure des Coproprietes et des Acces").
-- =========================================================================

CREATE TABLE propriete_lot (
    id                  UUID PRIMARY KEY,
    lot_id              UUID NOT NULL,
    contact_id          UUID NOT NULL,
    part_propriete      NUMERIC(5, 2) NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_propriete_lot_lot FOREIGN KEY (lot_id) REFERENCES lot (id),
    CONSTRAINT fk_propriete_lot_contact FOREIGN KEY (contact_id) REFERENCES contact (id),
    CONSTRAINT uk_propriete_lot_lot_contact UNIQUE (lot_id, contact_id)
);

CREATE INDEX idx_propriete_lot_lot_id ON propriete_lot (lot_id);
CREATE INDEX idx_propriete_lot_contact_id ON propriete_lot (contact_id);

CREATE TABLE membre_syndic (
    id                  UUID PRIMARY KEY,
    copropriete_id      UUID NOT NULL,
    contact_id          UUID NOT NULL,
    role_gestion        VARCHAR(30) NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_membre_syndic_copropriete FOREIGN KEY (copropriete_id) REFERENCES copropriete (id),
    CONSTRAINT fk_membre_syndic_contact FOREIGN KEY (contact_id) REFERENCES contact (id),
    CONSTRAINT uk_membre_syndic_copropriete_contact_role UNIQUE (copropriete_id, contact_id, role_gestion)
);

CREATE INDEX idx_membre_syndic_copropriete_id ON membre_syndic (copropriete_id);
CREATE INDEX idx_membre_syndic_contact_id ON membre_syndic (contact_id);
