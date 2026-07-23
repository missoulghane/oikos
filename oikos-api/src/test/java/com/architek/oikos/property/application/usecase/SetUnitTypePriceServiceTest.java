package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.SetUnitTypePriceCommand;
import com.architek.oikos.property.domain.exception.UnitTypeDefinitionNotFoundException;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.model.UnitTypePricing;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.repository.UnitTypePricingRepository;
import com.architek.oikos.property.domain.valueobject.Price;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.domain.valueobject.UnitTypePricingId;

@ExtendWith(MockitoExtension.class)
class SetUnitTypePriceServiceTest {

    @Mock
    private UnitTypePricingRepository unitTypePricingRepository;

    @Mock
    private UnitTypeDefinitionRepository unitTypeDefinitionRepository;

    private SetUnitTypePriceService newService() {
        return new SetUnitTypePriceService(unitTypePricingRepository, unitTypeDefinitionRepository);
    }

    @Test
    void setting_a_price_for_a_new_unit_type_creates_it() {
        PropertyId propertyId = PropertyId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        when(unitTypeDefinitionRepository.findById(unitTypeId))
                .thenReturn(Optional.of(UnitTypeDefinition.create(unitTypeId, propertyId, "Appartement")));
        when(unitTypePricingRepository.findByUnitTypeId(unitTypeId)).thenReturn(Optional.empty());
        when(unitTypePricingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var view = newService().set(new SetUnitTypePriceCommand(propertyId, unitTypeId,
                Price.of(new BigDecimal("300.00"))));

        assertThat(view.unitTypeId()).isEqualTo(unitTypeId);
        assertThat(view.unitTypeName()).isEqualTo("Appartement");
        assertThat(view.price().value()).isEqualByComparingTo("300.00");
    }

    @Test
    void setting_a_price_for_an_already_configured_unit_type_updates_it() {
        PropertyId propertyId = PropertyId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        UnitTypePricing existing = UnitTypePricing.create(UnitTypePricingId.newId(), propertyId, unitTypeId,
                Price.of(new BigDecimal("300.00")));
        when(unitTypeDefinitionRepository.findById(unitTypeId))
                .thenReturn(Optional.of(UnitTypeDefinition.create(unitTypeId, propertyId, "Appartement")));
        when(unitTypePricingRepository.findByUnitTypeId(unitTypeId)).thenReturn(Optional.of(existing));
        when(unitTypePricingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var view = newService().set(new SetUnitTypePriceCommand(propertyId, unitTypeId,
                Price.of(new BigDecimal("350.00"))));

        assertThat(view.id()).isEqualTo(existing.getId());
        assertThat(view.price().value()).isEqualByComparingTo("350.00");
    }

    @Test
    void setting_a_price_for_an_unknown_unit_type_is_rejected() {
        PropertyId propertyId = PropertyId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        when(unitTypeDefinitionRepository.findById(unitTypeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().set(new SetUnitTypePriceCommand(propertyId, unitTypeId,
                Price.of(new BigDecimal("300.00")))))
                .isInstanceOf(UnitTypeDefinitionNotFoundException.class);
    }

    @Test
    void setting_a_price_for_a_unit_type_belonging_to_another_property_is_rejected() {
        PropertyId propertyId = PropertyId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        when(unitTypeDefinitionRepository.findById(unitTypeId))
                .thenReturn(Optional.of(UnitTypeDefinition.create(unitTypeId, PropertyId.newId(), "Appartement")));

        assertThatThrownBy(() -> newService().set(new SetUnitTypePriceCommand(propertyId, unitTypeId,
                Price.of(new BigDecimal("300.00")))))
                .isInstanceOf(UnitTypeDefinitionNotFoundException.class);
    }
}
