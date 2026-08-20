package com.architek.oikos.property.domain.model;

import java.util.Objects;

import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BuildingId;

/**
 * Batiment physique appartenant a une property. Immutable: toute mutation
 * retourne une nouvelle instance. Semantique d'entite: equals/hashCode se
 * basent sur l'identite (id), pas sur les valeurs.
 */
public final class Building {

    private final BuildingId id;
    private final PropertyId propertyId;
    private final String name;
    private final Integer floorCount;

    private Building(BuildingId id, PropertyId propertyId, String name, Integer floorCount) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.name = requireNonBlank(name, "name");
        this.floorCount = Objects.requireNonNull(floorCount, "floorCount must not be null");
        if (floorCount < 0) {
            throw new IllegalArgumentException("floorCount must not be negative");
        }
    }

    public static Building create(BuildingId id, PropertyId propertyId, String name, Integer floorCount) {
        return new Building(id, propertyId, name, floorCount);
    }

    public static Building reconstruct(BuildingId id, PropertyId propertyId, String name, Integer floorCount) {
        return new Building(id, propertyId, name, floorCount);
    }

    /**
     * Renomme le batiment et corrige son nombre d'etages. La copropriete
     * d'appartenance, elle, ne bouge pas : deplacer un batiment d'une copropriete
     * a une autre emporterait ses lots, leurs proprietaires et leurs appels de
     * charges - ce n'est pas une modification de fiche.
     */
    public Building withDetails(String newName, Integer newFloorCount) {
        return new Building(id, propertyId, newName, newFloorCount);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public BuildingId getId() {
        return id;
    }

    public PropertyId getPropertyId() {
        return propertyId;
    }

    public String getName() {
        return name;
    }

    public Integer getFloorCount() {
        return floorCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Building other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
