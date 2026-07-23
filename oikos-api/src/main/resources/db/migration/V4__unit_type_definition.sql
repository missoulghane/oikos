-- =========================================================================
-- Replaces the fixed global UnitType enum with a per-property, user-defined
-- unit type catalog: each property has its own set of "unit_type_definition"
-- rows (e.g. "Appartement", "Box"), always seeded with one default "OTHERS"
-- row at property creation (see CreatePropertyService/ConfigurePropertyService).
-- unit.unit_type_id and unit_type_pricing.unit_type_id now reference this
-- table by FK instead of storing the old enum value directly.
-- =========================================================================

CREATE TABLE unit_type_definition (
    id                  UUID PRIMARY KEY,
    property_id         UUID NOT NULL,
    name                VARCHAR(50) NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_unit_type_definition_property FOREIGN KEY (property_id) REFERENCES property (id),
    CONSTRAINT uk_unit_type_definition_property_name UNIQUE (property_id, name)
);

CREATE INDEX idx_unit_type_definition_property_id ON unit_type_definition (property_id);

-- unit_type_pricing: replace the enum column with a FK. A price without a
-- type has no meaning, so removing a type cascades to its price row.
ALTER TABLE unit_type_pricing DROP CONSTRAINT uk_unit_type_pricing_property_unit_type;
ALTER TABLE unit_type_pricing DROP COLUMN unit_type;
ALTER TABLE unit_type_pricing ADD COLUMN unit_type_id UUID NOT NULL;
ALTER TABLE unit_type_pricing
    ADD CONSTRAINT fk_unit_type_pricing_unit_type FOREIGN KEY (unit_type_id)
    REFERENCES unit_type_definition (id) ON DELETE CASCADE;
ALTER TABLE unit_type_pricing ADD CONSTRAINT uk_unit_type_pricing_unit_type UNIQUE (unit_type_id);

-- unit: replace the enum column with a FK. No cascade here - a unit type
-- still assigned to at least one unit cannot be removed (enforced by
-- RemoveUnitTypeDefinitionService before this constraint would ever fire).
ALTER TABLE unit DROP COLUMN unit_type;
ALTER TABLE unit ADD COLUMN unit_type_id UUID NOT NULL;
ALTER TABLE unit
    ADD CONSTRAINT fk_unit_unit_type FOREIGN KEY (unit_type_id) REFERENCES unit_type_definition (id);

CREATE INDEX idx_unit_type_pricing_unit_type_id ON unit_type_pricing (unit_type_id);
CREATE INDEX idx_unit_unit_type_id ON unit (unit_type_id);
