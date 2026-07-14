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
}
