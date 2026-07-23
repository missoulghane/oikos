package com.architek.oikos.property.domain.model;

import java.util.Objects;

import com.architek.oikos.property.domain.valueobject.PropertyId;

/**
 * Entite racine representant la property geree. Une property peut exister
 * sans building (ex: inscription d'un property manager, voir
 * PropertyProvisioningAdapter) ; des buildings peuvent lui etre rattaches
 * plus tard via AddBuildingUseCase. Le endpoint public POST /properties
 * continue neanmoins d'exiger un premier building a la creation (voir
 * CreatePropertyRequest), par choix de ce point d'entree specifique et non
 * par contrainte de cet agregat.
 * Immutable: toute mutation retourne une nouvelle instance. Semantique
 * d'entite: equals/hashCode se basent sur l'identite (id), pas sur les valeurs.
 */
public final class Property {

    private final PropertyId id;
    private final String name;
    private final String address;

    private Property(PropertyId id, String name, String address) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = requireNonBlank(name, "name");
        this.address = requireNonBlank(address, "address");
    }

    public static Property create(PropertyId id, String name, String address) {
        return new Property(id, name, address);
    }

    public static Property reconstruct(PropertyId id, String name, String address) {
        return new Property(id, name, address);
    }

    public Property withDetails(String newName, String newAddress) {
        return new Property(id, newName, newAddress);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public PropertyId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Property other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
