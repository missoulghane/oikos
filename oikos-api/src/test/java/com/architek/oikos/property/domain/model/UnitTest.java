package com.architek.oikos.property.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.Shares;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

class UnitTest {

    @Test
    void create_builds_a_unit_with_the_given_fields() {
        BuildingId buildingId = BuildingId.newId();
        PropertyId propertyId = PropertyId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        Unit unit = Unit.create(UnitId.newId(), buildingId, propertyId, "A12", unitTypeId, Shares.of(new BigDecimal("150")));

        assertThat(unit.getBuildingId()).isEqualTo(buildingId);
        assertThat(unit.getPropertyId()).isEqualTo(propertyId);
        assertThat(unit.getUnitNumber()).isEqualTo("A12");
        assertThat(unit.getUnitTypeId()).isEqualTo(unitTypeId);
        assertThat(unit.getShares().value()).isEqualByComparingTo("150");
    }

    @Test
    void shares_rejects_a_negative_value() {
        assertThatThrownBy(() -> Shares.of(new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equality_is_based_on_identity_not_on_field_values() {
        UnitId id = UnitId.newId();
        Unit a = Unit.create(id, BuildingId.newId(), PropertyId.newId(), "A12", UnitTypeDefinitionId.newId(),
                Shares.of(BigDecimal.TEN));
        Unit b = Unit.create(id, BuildingId.newId(), PropertyId.newId(), "B34", UnitTypeDefinitionId.newId(),
                Shares.of(BigDecimal.ONE));

        assertThat(a).isEqualTo(b);
    }
}
