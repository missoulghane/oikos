package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.RemoveUnitTypeDefinitionCommand;
import com.architek.oikos.property.domain.exception.UnitTypeDefinitionNotFoundException;
import com.architek.oikos.property.domain.exception.UnitTypeInUseException;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

@ExtendWith(MockitoExtension.class)
class RemoveUnitTypeDefinitionServiceTest {

    @Mock
    private UnitTypeDefinitionRepository unitTypeDefinitionRepository;

    @Mock
    private UnitRepository unitRepository;

    private RemoveUnitTypeDefinitionService newService() {
        return new RemoveUnitTypeDefinitionService(unitTypeDefinitionRepository, unitRepository);
    }

    @Test
    void removing_an_unused_unit_type_deletes_it() {
        UnitTypeDefinitionId id = UnitTypeDefinitionId.newId();
        UnitTypeDefinition unitType = UnitTypeDefinition.create(id, PropertyId.newId(), "Duplex");
        when(unitTypeDefinitionRepository.findById(id)).thenReturn(Optional.of(unitType));
        when(unitRepository.existsByUnitTypeId(id)).thenReturn(false);

        newService().remove(new RemoveUnitTypeDefinitionCommand(id));

        verify(unitTypeDefinitionRepository).deleteById(id);
    }

    @Test
    void removing_a_unit_type_still_assigned_to_a_unit_is_rejected() {
        UnitTypeDefinitionId id = UnitTypeDefinitionId.newId();
        UnitTypeDefinition unitType = UnitTypeDefinition.create(id, PropertyId.newId(), "Appartement");
        when(unitTypeDefinitionRepository.findById(id)).thenReturn(Optional.of(unitType));
        when(unitRepository.existsByUnitTypeId(id)).thenReturn(true);

        assertThatThrownBy(() -> newService().remove(new RemoveUnitTypeDefinitionCommand(id)))
                .isInstanceOf(UnitTypeInUseException.class);
    }

    @Test
    void removing_an_unknown_unit_type_throws() {
        UnitTypeDefinitionId id = UnitTypeDefinitionId.newId();
        when(unitTypeDefinitionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().remove(new RemoveUnitTypeDefinitionCommand(id)))
                .isInstanceOf(UnitTypeDefinitionNotFoundException.class);
    }
}
