package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.query.ListUnitOwnershipsByUnitQuery;
import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.OwnershipShare;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ListUnitOwnershipsByUnitServiceTest {

    @Mock
    private UnitOwnershipRepository unitOwnershipRepository;

    @Test
    void listing_owners_maps_the_repository_entries_to_views() {
        UnitId unitId = UnitId.newId();
        UnitOwnership unitOwnership = UnitOwnership.create(UnitOwnershipId.newId(), unitId, EntityId.newId(),
                OwnershipShare.of(new BigDecimal("50")));
        when(unitOwnershipRepository.findAllByUnitId(unitId)).thenReturn(List.of(unitOwnership));

        var views = new ListUnitOwnershipsByUnitService(unitOwnershipRepository)
                .listUnitOwnerships(new ListUnitOwnershipsByUnitQuery(unitId));

        assertThat(views).extracting(view -> view.ownershipShare()).containsExactly(new BigDecimal("50"));
    }
}
