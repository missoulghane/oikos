package com.architek.oikos.property.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BuildingId;

class BuildingTest {

    @Test
    void create_builds_an_building_with_the_given_fields() {
        PropertyId propertyId = PropertyId.newId();
        Building building = Building.create(BuildingId.newId(), propertyId, "Batiment A", 5);

        assertThat(building.getPropertyId()).isEqualTo(propertyId);
        assertThat(building.getName()).isEqualTo("Batiment A");
        assertThat(building.getFloorCount()).isEqualTo(5);
    }

    @Test
    void create_rejects_a_negative_floor_count() {
        assertThatThrownBy(() -> Building.create(BuildingId.newId(), PropertyId.newId(), "Batiment A", -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equality_is_based_on_identity_not_on_field_values() {
        BuildingId id = BuildingId.newId();
        Building a = Building.create(id, PropertyId.newId(), "Batiment A", 5);
        Building b = Building.create(id, PropertyId.newId(), "Batiment B", 10);

        assertThat(a).isEqualTo(b);
    }
}
