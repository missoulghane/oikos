package com.architek.oikos.property.domain.model;

import java.util.Objects;

import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

/**
 * Type de lot propre a une property (ex: "Appartement", "Duplex"), remplace
 * l'ancien enum global UnitType: chaque property definit ses propres types.
 * Une ligne "OTHERS" est systematiquement creee a la creation de la property
 * (voir CreatePropertyService/ConfigurePropertyService) - elle n'est pas
 * protegee, elle peut etre retiree comme n'importe quel autre type tant
 * qu'aucun Unit ne la reference. Immutable: semantique d'entite, equals/
 * hashCode se basent sur l'identite (id), pas sur les valeurs.
 */
public final class UnitTypeDefinition {

    public static final String DEFAULT_NAME = "OTHERS";

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
