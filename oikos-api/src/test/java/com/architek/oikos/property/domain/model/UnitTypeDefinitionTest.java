package com.architek.oikos.property.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

class UnitTypeDefinitionTest {

    @Test
    void create_builds_a_unit_type_definition_with_the_given_fields() {
        PropertyId propertyId = PropertyId.newId();
        UnitTypeDefinition unitType = UnitTypeDefinition.create(UnitTypeDefinitionId.newId(), propertyId, "Appartement");

        assertThat(unitType.getPropertyId()).isEqualTo(propertyId);
        assertThat(unitType.getName()).isEqualTo("Appartement");
    }

    @Test
    void a_blank_name_is_rejected() {
        assertThatThrownBy(() -> UnitTypeDefinition.create(UnitTypeDefinitionId.newId(), PropertyId.newId(), " "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equality_is_based_on_identity_not_on_field_values() {
        UnitTypeDefinitionId id = UnitTypeDefinitionId.newId();
        UnitTypeDefinition a = UnitTypeDefinition.create(id, PropertyId.newId(), "Appartement");
        UnitTypeDefinition b = UnitTypeDefinition.create(id, PropertyId.newId(), "Box");

        assertThat(a).isEqualTo(b);
    }
}
