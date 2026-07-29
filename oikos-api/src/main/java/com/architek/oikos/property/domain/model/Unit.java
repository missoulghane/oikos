package com.architek.oikos.property.domain.model;

import java.util.Objects;

import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.Shares;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

/**
 * Unite privative au sein d'un building. propertyId est denormalise depuis
 * building.propertyId (cf. AddUnitService), pour permettre les FK composites
 * sur unit_ownership sans depiler jusqu'a Building a chaque fois. Immutable:
 * toute mutation retourne une nouvelle instance. Semantique d'entite:
 * equals/hashCode se basent sur l'identite (id), pas sur les valeurs.
 */
public final class Unit {

    private final UnitId id;
    private final BuildingId buildingId;
    private final PropertyId propertyId;
    private final String unitNumber;
    private final UnitTypeDefinitionId unitTypeId;
    private final Shares shares;

    private Unit(UnitId id, BuildingId buildingId, PropertyId propertyId, String unitNumber,
                  UnitTypeDefinitionId unitTypeId, Shares shares) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.buildingId = Objects.requireNonNull(buildingId, "buildingId must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.unitNumber = requireNonBlank(unitNumber, "unitNumber");
        this.unitTypeId = Objects.requireNonNull(unitTypeId, "unitTypeId must not be null");
        this.shares = Objects.requireNonNull(shares, "shares must not be null");
    }

    public static Unit create(UnitId id, BuildingId buildingId, PropertyId propertyId, String unitNumber,
                               UnitTypeDefinitionId unitTypeId, Shares shares) {
        return new Unit(id, buildingId, propertyId, unitNumber, unitTypeId, shares);
    }

    public static Unit reconstruct(UnitId id, BuildingId buildingId, PropertyId propertyId, String unitNumber,
                                    UnitTypeDefinitionId unitTypeId, Shares shares) {
        return new Unit(id, buildingId, propertyId, unitNumber, unitTypeId, shares);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public UnitId getId() {
        return id;
    }

    public BuildingId getBuildingId() {
        return buildingId;
    }

    public PropertyId getPropertyId() {
        return propertyId;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public UnitTypeDefinitionId getUnitTypeId() {
        return unitTypeId;
    }

    public Shares getShares() {
        return shares;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Unit other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
