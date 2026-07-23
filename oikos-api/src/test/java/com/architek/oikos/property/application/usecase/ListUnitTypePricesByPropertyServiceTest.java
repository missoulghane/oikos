package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.query.ListUnitTypePricesByPropertyQuery;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.model.UnitTypePricing;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.repository.UnitTypePricingRepository;
import com.architek.oikos.property.domain.valueobject.Price;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.domain.valueobject.UnitTypePricingId;

@ExtendWith(MockitoExtension.class)
class ListUnitTypePricesByPropertyServiceTest {

    @Mock
    private UnitTypePricingRepository unitTypePricingRepository;

    @Mock
    private UnitTypeDefinitionRepository unitTypeDefinitionRepository;

    @Test
    void listing_prices_maps_the_repository_entries_to_views_with_the_resolved_unit_type_name() {
        PropertyId propertyId = PropertyId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        UnitTypePricing pricing = UnitTypePricing.create(UnitTypePricingId.newId(), propertyId, unitTypeId,
                Price.of(new BigDecimal("300.00")));
        when(unitTypeDefinitionRepository.findAllByPropertyId(propertyId))
                .thenReturn(List.of(UnitTypeDefinition.create(unitTypeId, propertyId, "Appartement")));
        when(unitTypePricingRepository.findAllByPropertyId(propertyId)).thenReturn(List.of(pricing));

        var views = new ListUnitTypePricesByPropertyService(unitTypePricingRepository, unitTypeDefinitionRepository)
                .listUnitTypePrices(new ListUnitTypePricesByPropertyQuery(propertyId));

        assertThat(views).extracting(view -> view.unitTypeName()).containsExactly("Appartement");
    }
}
