package com.architek.oikos.property.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.property.domain.model.UnitTypePricing;
import com.architek.oikos.property.domain.valueobject.Price;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.domain.valueobject.UnitTypePricingId;
import com.architek.oikos.property.infrastructure.mapper.UnitTypePricingPersistenceMapperImpl;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UnitTypePricingRepositoryAdapter.class, UnitTypePricingPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class UnitTypePricingRepositoryAdapterDataJpaTest {

    @Autowired
    private UnitTypePricingRepositoryAdapter adapter;

    @Test
    void saves_and_finds_a_pricing_by_unit_type_id() {
        PropertyId propertyId = PropertyId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        UnitTypePricing pricing = UnitTypePricing.create(UnitTypePricingId.newId(), propertyId, unitTypeId,
                Price.of(new BigDecimal("300.00")));

        adapter.save(pricing);

        assertThat(adapter.findByUnitTypeId(unitTypeId)).isPresent()
                .get().extracting(found -> found.getPrice().value()).isEqualTo(new BigDecimal("300.00"));
    }

    @Test
    void findAllByPropertyId_returns_only_entries_for_that_property() {
        PropertyId propertyId = PropertyId.newId();
        adapter.save(UnitTypePricing.create(UnitTypePricingId.newId(), propertyId, UnitTypeDefinitionId.newId(),
                Price.of(new BigDecimal("300.00"))));
        adapter.save(UnitTypePricing.create(UnitTypePricingId.newId(), PropertyId.newId(), UnitTypeDefinitionId.newId(),
                Price.of(new BigDecimal("500.00"))));

        assertThat(adapter.findAllByPropertyId(propertyId)).hasSize(1);
    }

    @Test
    void deleteByUnitTypeId_removes_the_entry() {
        PropertyId propertyId = PropertyId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        adapter.save(UnitTypePricing.create(UnitTypePricingId.newId(), propertyId, unitTypeId,
                Price.of(new BigDecimal("100.00"))));

        adapter.deleteByUnitTypeId(unitTypeId);

        assertThat(adapter.findByUnitTypeId(unitTypeId)).isEmpty();
    }
}
