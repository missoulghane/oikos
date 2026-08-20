package com.architek.oikos.property.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.architek.oikos.property.domain.valueobject.PropertyId;

class PropertyTest {

    @Test
    void create_builds_a_property_with_the_given_fields() {
        Property property = Property.create(PropertyId.newId(), "Copro Laumiere", "33 Avenue de Laumiere");

        assertThat(property.getName()).isEqualTo("Copro Laumiere");
        assertThat(property.getAddress()).isEqualTo("33 Avenue de Laumiere");
    }

    @Test
    void create_rejects_a_blank_nom() {
        assertThatThrownBy(() -> Property.create(PropertyId.newId(), " ", "address"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equality_is_based_on_identity_not_on_field_values() {
        PropertyId id = PropertyId.newId();
        Property a = Property.create(id, "Copro A", "Address A");
        Property b = Property.create(id, "Copro B", "Address B");

        assertThat(a).isEqualTo(b);
    }

    @Test
    void a_property_can_carry_a_city_beside_its_address() {
        Property property = Property.create(PropertyId.newId(), "Copro Laumiere", "33 Avenue de Laumiere", "Casablanca");

        assertThat(property.getCity()).contains("Casablanca");
    }

    @Test
    void a_property_without_a_city_is_a_normal_property() {
        // L'état de toutes les copropriétés antérieures au champ, et de celles
        // qu'une inscription provisionne sans le demander.
        Property property = Property.create(PropertyId.newId(), "Copro Laumiere", "33 Avenue de Laumiere");

        assertThat(property.getCity()).isEmpty();
    }

    @Test
    void a_blank_city_is_the_same_as_no_city() {
        // Un champ vidé dans le formulaire ne doit pas laisser une chaîne vide en
        // base, que l'affichage rendrait par un blanc au lieu d'un « — ».
        Property property = Property.create(PropertyId.newId(), "Copro Laumiere", "33 Avenue de Laumiere", "   ");

        assertThat(property.getCity()).isEmpty();
    }

    @Test
    void with_details_returns_a_new_instance_with_updated_name_and_address() {
        Property property = Property.create(PropertyId.newId(), "Copro Laumiere", "33 Avenue de Laumiere");

        Property updated = property.withDetails("Copro Renamed", "New address", "Rabat");

        assertThat(updated.getId()).isEqualTo(property.getId());
        assertThat(updated.getName()).isEqualTo("Copro Renamed");
        assertThat(updated.getAddress()).isEqualTo("New address");
    }
}
