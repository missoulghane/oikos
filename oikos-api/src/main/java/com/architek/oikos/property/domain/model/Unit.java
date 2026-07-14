package com.architek.oikos.property.domain.model;

import java.util.Objects;

import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.Shares;
import com.architek.oikos.property.domain.valueobject.UnitType;

/**
 * Unite privative au sein d'un building. Immutable: toute mutation retourne
 * une nouvelle instance. Semantique d'entite: equals/hashCode se basent sur
 * l'identite (id), pas sur les valeurs.
 */
public final class Unit {

    private final UnitId id;
    private final BuildingId buildingId;
    private final String unitNumber;
    private final UnitType unitType;
    private final Shares shares;

    private Unit(UnitId id, BuildingId buildingId, String unitNumber, UnitType unitType, Shares shares) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.buildingId = Objects.requireNonNull(buildingId, "buildingId must not be null");
        this.unitNumber = requireNonBlank(unitNumber, "unitNumber");
        this.unitType = Objects.requireNonNull(unitType, "unitType must not be null");
        this.shares = Objects.requireNonNull(shares, "shares must not be null");
    }

    public static Unit create(UnitId id, BuildingId buildingId, String unitNumber, UnitType unitType, Shares shares) {
        return new Unit(id, buildingId, unitNumber, unitType, shares);
    }

    public static Unit reconstruct(UnitId id, BuildingId buildingId, String unitNumber, UnitType unitType, Shares shares) {
        return new Unit(id, buildingId, unitNumber, unitType, shares);
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

    public String getUnitNumber() {
        return unitNumber;
    }

    public UnitType getUnitType() {
        return unitType;
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
