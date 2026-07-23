package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.RemoveUnitTypePriceCommand;
import com.architek.oikos.property.domain.exception.UnitTypePriceNotFoundException;
import com.architek.oikos.property.domain.model.UnitTypePricing;
import com.architek.oikos.property.domain.repository.UnitTypePricingRepository;
import com.architek.oikos.property.domain.valueobject.Price;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.domain.valueobject.UnitTypePricingId;

@ExtendWith(MockitoExtension.class)
class RemoveUnitTypePriceServiceTest {

    @Mock
    private UnitTypePricingRepository unitTypePricingRepository;

    @Test
    void removing_an_existing_entry_deletes_it() {
        PropertyId propertyId = PropertyId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        UnitTypePricing pricing = UnitTypePricing.create(UnitTypePricingId.newId(), propertyId, unitTypeId,
                Price.of(new BigDecimal("300.00")));
        when(unitTypePricingRepository.findByUnitTypeId(unitTypeId)).thenReturn(Optional.of(pricing));

        new RemoveUnitTypePriceService(unitTypePricingRepository)
                .remove(new RemoveUnitTypePriceCommand(propertyId, unitTypeId));

        verify(unitTypePricingRepository).deleteByUnitTypeId(unitTypeId);
    }

    @Test
    void removing_an_unconfigured_unit_type_throws() {
        PropertyId propertyId = PropertyId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        when(unitTypePricingRepository.findByUnitTypeId(unitTypeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new RemoveUnitTypePriceService(unitTypePricingRepository)
                        .remove(new RemoveUnitTypePriceCommand(propertyId, unitTypeId)))
                .isInstanceOf(UnitTypePriceNotFoundException.class);
    }

    @Test
    void removing_a_price_belonging_to_another_property_throws() {
        PropertyId propertyId = PropertyId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        UnitTypePricing pricing = UnitTypePricing.create(UnitTypePricingId.newId(), PropertyId.newId(), unitTypeId,
                Price.of(new BigDecimal("300.00")));
        when(unitTypePricingRepository.findByUnitTypeId(unitTypeId)).thenReturn(Optional.of(pricing));

        assertThatThrownBy(() -> new RemoveUnitTypePriceService(unitTypePricingRepository)
                        .remove(new RemoveUnitTypePriceCommand(propertyId, unitTypeId)))
                .isInstanceOf(UnitTypePriceNotFoundException.class);
    }
}
