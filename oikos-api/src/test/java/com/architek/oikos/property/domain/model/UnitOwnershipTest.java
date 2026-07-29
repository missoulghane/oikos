package com.architek.oikos.property.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.OwnershipShare;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class UnitOwnershipTest {

    @Test
    void create_builds_a_unit_ownership_with_the_given_fields() {
        UnitId unitId = UnitId.newId();
        EntityId partyId = EntityId.newId();
        PropertyId propertyId = PropertyId.newId();
        UnitOwnership unitOwnership = UnitOwnership.create(UnitOwnershipId.newId(), unitId, partyId, propertyId,
                OwnershipShare.of(BigDecimal.TEN));

        assertThat(unitOwnership.getUnitId()).isEqualTo(unitId);
        assertThat(unitOwnership.getPartyId()).isEqualTo(partyId);
        assertThat(unitOwnership.getPropertyId()).isEqualTo(propertyId);
        assertThat(unitOwnership.getOwnershipShare().value()).isEqualByComparingTo("10");
    }

    @Test
    void equality_is_based_on_identity_not_on_field_values() {
        UnitOwnershipId id = UnitOwnershipId.newId();
        UnitOwnership a = UnitOwnership.create(id, UnitId.newId(), EntityId.newId(), PropertyId.newId(),
                OwnershipShare.of(BigDecimal.TEN));
        UnitOwnership b = UnitOwnership.create(id, UnitId.newId(), EntityId.newId(), PropertyId.newId(),
                OwnershipShare.of(BigDecimal.ONE));

        assertThat(a).isEqualTo(b);
    }
}
