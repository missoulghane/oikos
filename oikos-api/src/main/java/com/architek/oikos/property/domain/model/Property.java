package com.architek.oikos.property.domain.model;

import java.util.Objects;

import com.architek.oikos.property.domain.valueobject.PropertyId;

/**
 * Entite racine representant la property geree. La regle "une property
 * doit posseder au moins un building" n'est pas verifiable au niveau de cet
 * agregat seul (probleme de l'oeuf et de la poule a la creation) : elle est
 * appliquee au niveau applicatif par une commande composite qui cree la
 * property et son premier building dans la meme transaction (voir
 * CreatePropertyService).
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
