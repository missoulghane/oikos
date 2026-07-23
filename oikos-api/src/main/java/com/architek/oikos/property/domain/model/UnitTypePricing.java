package com.architek.oikos.property.domain.model;

import java.util.Objects;

import com.architek.oikos.property.domain.valueobject.Price;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.domain.valueobject.UnitTypePricingId;

/**
 * Prix courant d'un UnitTypeDefinition pour une property (ex: 300 pour un
 * appartement). Au plus une entree par unitTypeId - voir
 * UnitTypePricingRepository (le type est deja rattache a une seule property,
 * la contrainte n'a donc pas besoin de porter sur (property, unitType)).
 * Immutable: toute mutation retourne une nouvelle instance. Semantique
 * d'entite: equals/hashCode se basent sur l'identite (id), pas sur les
 * valeurs.
 */
public final class UnitTypePricing {

    private final UnitTypePricingId id;
    private final PropertyId propertyId;
    private final UnitTypeDefinitionId unitTypeId;
    private final Price price;

    private UnitTypePricing(UnitTypePricingId id, PropertyId propertyId, UnitTypeDefinitionId unitTypeId, Price price) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.unitTypeId = Objects.requireNonNull(unitTypeId, "unitTypeId must not be null");
        this.price = Objects.requireNonNull(price, "price must not be null");
    }

    public static UnitTypePricing create(UnitTypePricingId id, PropertyId propertyId, UnitTypeDefinitionId unitTypeId,
                                           Price price) {
        return new UnitTypePricing(id, propertyId, unitTypeId, price);
    }

    public static UnitTypePricing reconstruct(UnitTypePricingId id, PropertyId propertyId,
                                                UnitTypeDefinitionId unitTypeId, Price price) {
        return new UnitTypePricing(id, propertyId, unitTypeId, price);
    }

    public UnitTypePricing withPrice(Price newPrice) {
        return new UnitTypePricing(id, propertyId, unitTypeId, newPrice);
    }

    public UnitTypePricingId getId() {
        return id;
    }

    public PropertyId getPropertyId() {
        return propertyId;
    }

    public UnitTypeDefinitionId getUnitTypeId() {
        return unitTypeId;
    }

    public Price getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof UnitTypePricing other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
