package com.architek.oikos.property.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.architek.oikos.property.domain.valueobject.Price;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.domain.valueobject.UnitTypePricingId;

class UnitTypePricingTest {

    @Test
    void create_builds_a_unit_type_pricing_with_the_given_fields() {
        PropertyId propertyId = PropertyId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        Price price = Price.of(new BigDecimal("300.00"));
        UnitTypePricing pricing = UnitTypePricing.create(UnitTypePricingId.newId(), propertyId, unitTypeId, price);

        assertThat(pricing.getPropertyId()).isEqualTo(propertyId);
        assertThat(pricing.getUnitTypeId()).isEqualTo(unitTypeId);
        assertThat(pricing.getPrice()).isEqualTo(price);
    }

    @Test
    void withPrice_returns_a_new_instance_with_the_updated_price() {
        UnitTypePricing pricing = UnitTypePricing.create(UnitTypePricingId.newId(), PropertyId.newId(),
                UnitTypeDefinitionId.newId(), Price.of(new BigDecimal("100.00")));

        UnitTypePricing updated = pricing.withPrice(Price.of(new BigDecimal("150.00")));

        assertThat(updated.getPrice().value()).isEqualByComparingTo("150.00");
        assertThat(updated.getId()).isEqualTo(pricing.getId());
        assertThat(pricing.getPrice().value()).isEqualByComparingTo("100.00");
    }

    @Test
    void equality_is_based_on_identity_not_on_field_values() {
        UnitTypePricingId id = UnitTypePricingId.newId();
        UnitTypePricing a = UnitTypePricing.create(id, PropertyId.newId(), UnitTypeDefinitionId.newId(),
                Price.of(new BigDecimal("300.00")));
        UnitTypePricing b = UnitTypePricing.create(id, PropertyId.newId(), UnitTypeDefinitionId.newId(),
                Price.of(new BigDecimal("500.00")));

        assertThat(a).isEqualTo(b);
    }
}
