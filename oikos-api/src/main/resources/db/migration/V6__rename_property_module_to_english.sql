-- =========================================================================
-- Renames the whole "copropriete" module to English nomenclature (see
-- docs/ARCHITECTURE.md rule 2 and docs/NOMENCLATURE.md): tables, columns,
-- constraints, indexes, and the enum values stored as plain VARCHAR via
-- @Enumerated(EnumType.STRING). No environment has data in these tables yet,
-- but the data UPDATEs are included for correctness regardless.
-- =========================================================================

ALTER TABLE copropriete RENAME TO property;
ALTER TABLE property RENAME COLUMN nom TO name;
ALTER TABLE property RENAME COLUMN adresse TO address;

ALTER TABLE immeuble RENAME TO building;
ALTER TABLE building RENAME COLUMN copropriete_id TO property_id;
ALTER TABLE building RENAME COLUMN nom TO name;
ALTER TABLE building RENAME COLUMN nombre_etages TO floor_count;
ALTER TABLE building RENAME CONSTRAINT fk_immeuble_copropriete TO fk_building_property;
ALTER INDEX idx_immeuble_copropriete_id RENAME TO idx_building_property_id;

ALTER TABLE lot RENAME TO unit;
ALTER TABLE unit RENAME COLUMN immeuble_id TO building_id;
ALTER TABLE unit RENAME COLUMN numero_lot TO unit_number;
ALTER TABLE unit RENAME COLUMN type_lot TO unit_type;
ALTER TABLE unit RENAME COLUMN tantiemes TO shares;
ALTER TABLE unit RENAME CONSTRAINT fk_lot_immeuble TO fk_unit_building;
ALTER INDEX idx_lot_immeuble_id RENAME TO idx_unit_building_id;

UPDATE unit SET unit_type = CASE unit_type
    WHEN 'APPARTEMENT' THEN 'APARTMENT'
    WHEN 'BUREAU' THEN 'OFFICE'
    WHEN 'COMMERCE' THEN 'COMMERCIAL'
    WHEN 'CAVE' THEN 'STORAGE'
    WHEN 'AUTRE' THEN 'OTHER'
    ELSE unit_type
END;

ALTER TABLE propriete_lot RENAME TO unit_ownership;
ALTER TABLE unit_ownership RENAME COLUMN lot_id TO unit_id;
ALTER TABLE unit_ownership RENAME COLUMN part_propriete TO ownership_share;
ALTER TABLE unit_ownership RENAME CONSTRAINT fk_propriete_lot_lot TO fk_unit_ownership_unit;
ALTER TABLE unit_ownership RENAME CONSTRAINT fk_propriete_lot_contact TO fk_unit_ownership_contact;
ALTER TABLE unit_ownership RENAME CONSTRAINT uk_propriete_lot_lot_contact TO uk_unit_ownership_unit_contact;
ALTER INDEX idx_propriete_lot_lot_id RENAME TO idx_unit_ownership_unit_id;
ALTER INDEX idx_propriete_lot_contact_id RENAME TO idx_unit_ownership_contact_id;

ALTER TABLE membre_syndic RENAME TO board_member;
ALTER TABLE board_member RENAME COLUMN copropriete_id TO property_id;
ALTER TABLE board_member RENAME COLUMN role_gestion TO board_role;
ALTER TABLE board_member RENAME CONSTRAINT fk_membre_syndic_copropriete TO fk_board_member_property;
ALTER TABLE board_member RENAME CONSTRAINT fk_membre_syndic_contact TO fk_board_member_contact;
ALTER TABLE board_member RENAME CONSTRAINT uk_membre_syndic_copropriete_contact_role TO uk_board_member_property_contact_role;
ALTER INDEX idx_membre_syndic_copropriete_id RENAME TO idx_board_member_property_id;
ALTER INDEX idx_membre_syndic_contact_id RENAME TO idx_board_member_contact_id;

UPDATE board_member SET board_role = CASE board_role
    WHEN 'TRESORIER' THEN 'TREASURER'
    WHEN 'SECRETAIRE' THEN 'SECRETARY'
    WHEN 'MEMBRE' THEN 'MEMBER'
    WHEN 'SYNDIC_BENEVOLE' THEN 'VOLUNTEER_MANAGER'
    ELSE board_role
END;
