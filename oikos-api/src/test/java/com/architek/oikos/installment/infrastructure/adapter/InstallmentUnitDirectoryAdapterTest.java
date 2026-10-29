package com.architek.oikos.installment.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.port.in.GetUnitUseCase;
import com.architek.oikos.property.application.port.in.ListUnitOwnershipsByUnitUseCase;
import com.architek.oikos.property.domain.exception.UnitNotFoundException;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.OwnershipStatus;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class InstallmentUnitDirectoryAdapterTest {

    private static final EntityId UNIT_ID = EntityId.newId();
    private static final EntityId PROPERTY_ID = EntityId.newId();

    @Mock
    private GetUnitUseCase getUnitUseCase;

    @Mock
    private ListUnitOwnershipsByUnitUseCase listUnitOwnershipsByUnitUseCase;

    private InstallmentUnitDirectoryAdapter newAdapter() {
        return new InstallmentUnitDirectoryAdapter(getUnitUseCase, listUnitOwnershipsByUnitUseCase);
    }

    private void givenUnitIn(EntityId propertyId) {
        when(getUnitUseCase.getUnit(any())).thenReturn(new UnitView(UnitId.of(UNIT_ID.value()), BuildingId.newId(),
                PropertyId.of(propertyId.value()), "A12", UnitTypeDefinitionId.newId(), "Appartement",
                new BigDecimal("100"), OwnershipStatus.AFFECTED, List.of(), null));
    }

    @Test
    void a_unit_of_the_property_exists_in_it() {
        givenUnitIn(PROPERTY_ID);

        assertThat(newAdapter().existsInProperty(UNIT_ID, PROPERTY_ID)).isTrue();
    }

    @Test
    void a_unit_of_another_property_does_not() {
        givenUnitIn(EntityId.newId());

        assertThat(newAdapter().existsInProperty(UNIT_ID, PROPERTY_ID)).isFalse();
    }

    @Test
    void an_unknown_unit_does_not() {
        when(getUnitUseCase.getUnit(any())).thenThrow(new UnitNotFoundException(UnitId.of(UNIT_ID.value())));

        assertThat(newAdapter().existsInProperty(UNIT_ID, PROPERTY_ID)).isFalse();
    }
}
