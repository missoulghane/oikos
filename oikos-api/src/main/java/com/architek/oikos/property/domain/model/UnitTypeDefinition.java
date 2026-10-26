package com.architek.oikos.property.domain.model;

import java.util.Objects;

import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

/**
 * Type de lot propre a une property (ex: "Appartement", "Duplex"), remplace
 * l'ancien enum global UnitType: chaque property definit ses propres types.
 * Aucun type n'est cree d'office avec la property: ses types sont ceux que
 * le syndic declare (wizard, onglet Configuration). Immutable: semantique d'entite, equals/
 * hashCode se basent sur l'identite (id), pas sur les valeurs.
 */
public final class UnitTypeDefinition {

    private final UnitTypeDefinitionId id;
    private final PropertyId propertyId;
    private final String name;

    private UnitTypeDefinition(UnitTypeDefinitionId id, PropertyId propertyId, String name) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.name = requireNonBlank(name);
    }

    public static UnitTypeDefinition create(UnitTypeDefinitionId id, PropertyId propertyId, String name) {
        return new UnitTypeDefinition(id, propertyId, name);
    }

    public static UnitTypeDefinition reconstruct(UnitTypeDefinitionId id, PropertyId propertyId, String name) {
        return new UnitTypeDefinition(id, propertyId, name);
    }

    private static String requireNonBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return value;
    }

    public UnitTypeDefinitionId getId() {
        return id;
    }

    public PropertyId getPropertyId() {
        return propertyId;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof UnitTypeDefinition other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
