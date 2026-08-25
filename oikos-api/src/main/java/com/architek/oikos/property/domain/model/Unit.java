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
 *
 * <p>floor est facultatif (null = etage inconnu): c'est l'etat de tous les
 * lots crees avant que le champ existe, et de ceux generes en masse par les
 * services de configuration, qui ne savent pas repartir les lots par etage.
 * La coherence avec building.floorCount se verifie la ou le Building est
 * connu (AddUnitService), pas ici.
 */
public final class Unit {

    private final UnitId id;
    private final BuildingId buildingId;
    private final PropertyId propertyId;
    private final String unitNumber;
    private final UnitTypeDefinitionId unitTypeId;
    private final Shares shares;
    private final Integer floor;

    private Unit(UnitId id, BuildingId buildingId, PropertyId propertyId, String unitNumber,
                  UnitTypeDefinitionId unitTypeId, Shares shares, Integer floor) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.buildingId = Objects.requireNonNull(buildingId, "buildingId must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.unitNumber = requireNonBlank(unitNumber, "unitNumber");
        this.unitTypeId = Objects.requireNonNull(unitTypeId, "unitTypeId must not be null");
        this.shares = Objects.requireNonNull(shares, "shares must not be null");
        this.floor = requireNonNegativeOrNull(floor);
    }

    /** Lot dont l'etage n'est pas renseigne - voir le commentaire de classe. */
    public static Unit create(UnitId id, BuildingId buildingId, PropertyId propertyId, String unitNumber,
                               UnitTypeDefinitionId unitTypeId, Shares shares) {
        return new Unit(id, buildingId, propertyId, unitNumber, unitTypeId, shares, null);
    }

    public static Unit create(UnitId id, BuildingId buildingId, PropertyId propertyId, String unitNumber,
                               UnitTypeDefinitionId unitTypeId, Shares shares, Integer floor) {
        return new Unit(id, buildingId, propertyId, unitNumber, unitTypeId, shares, floor);
    }

    public static Unit reconstruct(UnitId id, BuildingId buildingId, PropertyId propertyId, String unitNumber,
                                    UnitTypeDefinitionId unitTypeId, Shares shares) {
        return new Unit(id, buildingId, propertyId, unitNumber, unitTypeId, shares, null);
    }

    public static Unit reconstruct(UnitId id, BuildingId buildingId, PropertyId propertyId, String unitNumber,
                                    UnitTypeDefinitionId unitTypeId, Shares shares, Integer floor) {
        return new Unit(id, buildingId, propertyId, unitNumber, unitTypeId, shares, floor);
    }

    public Unit withShares(Shares newShares) {
        return new Unit(id, buildingId, propertyId, unitNumber, unitTypeId, newShares, floor);
    }

    /**
     * Numerotation des lots generes en masse (services de configuration): le
     * numero seul, le type restant affiche a cote par les consommateurs -
     * "N° 1", "N° 2"..., une suite qui repart de 1 pour chaque type de lot d'un
     * meme batiment.
     */
    public static String generatedNumber(int sequence) {
        return "N° " + sequence;
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static Integer requireNonNegativeOrNull(Integer value) {
        if (value != null && value < 0) {
            throw new IllegalArgumentException("floor must not be negative");
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

    /** Null quand l'etage n'est pas renseigne - voir le commentaire de classe. */
    public Integer getFloor() {
        return floor;
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
