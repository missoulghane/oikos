-- =========================================================================
-- Per-property pricing configuration: an optional price per UnitType for a
-- given property (e.g. APARTMENT: 300, GARAGE_BOX: 100). At most one row per
-- (property, unit_type); a unit type without a row simply has no configured
-- price (no default, no error).
-- =========================================================================

CREATE TABLE unit_type_pricing (
    id                  UUID PRIMARY KEY,
    property_id         UUID NOT NULL,
    unit_type           VARCHAR(20) NOT NULL,
    price               NUMERIC(12, 2) NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_unit_type_pricing_property FOREIGN KEY (property_id) REFERENCES property (id),
    CONSTRAINT uk_unit_type_pricing_property_unit_type UNIQUE (property_id, unit_type)
);

CREATE INDEX idx_unit_type_pricing_property_id ON unit_type_pricing (property_id);
