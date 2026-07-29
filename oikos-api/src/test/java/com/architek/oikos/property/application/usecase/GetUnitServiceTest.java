package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.query.GetUnitQuery;
import com.architek.oikos.property.domain.exception.UnitNotFoundException;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.OwnershipShare;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.property.domain.valueobject.OwnershipStatus;
import com.architek.oikos.property.domain.valueobject.Shares;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class GetUnitServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private UnitOwnershipRepository unitOwnershipRepository;

    @Mock
    private UnitTypeDefinitionRepository unitTypeDefinitionRepository;

    private GetUnitService newService() {
        return new GetUnitService(unitRepository, unitOwnershipRepository, unitTypeDefinitionRepository);
    }

    @Test
    void a_unit_without_any_owner_is_labelled_non_affecte() {
        UnitId id = UnitId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        when(unitRepository.findById(id)).thenReturn(Optional.of(
                Unit.create(id, BuildingId.newId(), PropertyId.newId(), "A12", unitTypeId, Shares.of(new BigDecimal("150")))));
        when(unitOwnershipRepository.findAllByUnitId(id)).thenReturn(List.of());
        when(unitTypeDefinitionRepository.findById(unitTypeId))
                .thenReturn(Optional.of(UnitTypeDefinition.create(unitTypeId, PropertyId.newId(), "Appartement")));

        var view = newService().getUnit(new GetUnitQuery(id));

        assertThat(view.unitNumber()).isEqualTo("A12");
        assertThat(view.unitTypeName()).isEqualTo("Appartement");
        assertThat(view.ownershipStatus()).isEqualTo(OwnershipStatus.NOT_AFFECTED);
    }

    @Test
    void a_unit_with_at_least_one_owner_is_labelled_affecte() {
        UnitId id = UnitId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        when(unitRepository.findById(id)).thenReturn(Optional.of(
                Unit.create(id, BuildingId.newId(), PropertyId.newId(), "A12", unitTypeId, Shares.of(new BigDecimal("150")))));
        when(unitOwnershipRepository.findAllByUnitId(id)).thenReturn(List.of(
                UnitOwnership.create(UnitOwnershipId.newId(), id, EntityId.newId(), PropertyId.newId(),
                        OwnershipShare.of(BigDecimal.TEN))));
        when(unitTypeDefinitionRepository.findById(unitTypeId))
                .thenReturn(Optional.of(UnitTypeDefinition.create(unitTypeId, PropertyId.newId(), "Appartement")));

        var view = newService().getUnit(new GetUnitQuery(id));

        assertThat(view.ownershipStatus()).isEqualTo(OwnershipStatus.AFFECTED);
    }

    @Test
    void getting_a_missing_unit_throws() {
        UnitId id = UnitId.newId();
        when(unitRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().getUnit(new GetUnitQuery(id)))
                .isInstanceOf(UnitNotFoundException.class);
    }
}
