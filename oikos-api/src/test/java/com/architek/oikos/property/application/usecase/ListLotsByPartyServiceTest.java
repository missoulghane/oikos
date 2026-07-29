package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.dto.PartyLotView;
import com.architek.oikos.property.application.port.out.PartyDetails;
import com.architek.oikos.property.application.port.out.PartyDirectoryPort;
import com.architek.oikos.property.application.query.ListLotsByPartyQuery;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.OwnershipShare;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.Shares;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;

@ExtendWith(MockitoExtension.class)
class ListLotsByPartyServiceTest {

    @Mock
    private PartyDirectoryPort partyDirectoryPort;

    @Mock
    private UnitOwnershipRepository unitOwnershipRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private PropertyRepository propertyRepository;

    private ListLotsByPartyService newService() {
        return new ListLotsByPartyService(partyDirectoryPort, unitOwnershipRepository, unitRepository,
                buildingRepository, propertyRepository);
    }

    @Test
    void listing_lots_resolves_unit_building_and_property_for_each_ownership() {
        EntityId partyId = EntityId.newId();
        when(partyDirectoryPort.getPartyById(partyId))
                .thenReturn(new PartyDetails("Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane.doe@example.com"), null));

        UnitId unitId = UnitId.newId();
        PropertyId propertyId = PropertyId.newId();
        UnitOwnership unitOwnership = UnitOwnership.create(UnitOwnershipId.newId(), unitId, partyId, propertyId,
                OwnershipShare.of(new BigDecimal("50")));
        when(unitOwnershipRepository.findAllByPartyId(partyId)).thenReturn(List.of(unitOwnership));

        BuildingId buildingId = BuildingId.newId();
        Unit unit = Unit.create(unitId, buildingId, propertyId, "A12", UnitTypeDefinitionId.newId(), Shares.of(BigDecimal.TEN));
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(unit));

        Building building = Building.create(buildingId, propertyId, "Bâtiment A", 3);
        when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));

        Property property = Property.create(propertyId, "Copro Test", "1 rue de la Paix");
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        List<PartyLotView> lots = newService().listLots(new ListLotsByPartyQuery(partyId));

        assertThat(lots).hasSize(1);
        PartyLotView lot = lots.get(0);
        assertThat(lot.unitNumber()).isEqualTo("A12");
        assertThat(lot.buildingName()).isEqualTo("Bâtiment A");
        assertThat(lot.propertyName()).isEqualTo("Copro Test");
        assertThat(lot.ownershipShare()).isEqualByComparingTo("50");
    }

    @Test
    void listing_lots_of_a_party_owning_nothing_returns_an_empty_list() {
        EntityId partyId = EntityId.newId();
        when(partyDirectoryPort.getPartyById(partyId))
                .thenReturn(new PartyDetails("Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane.doe@example.com"), null));
        when(unitOwnershipRepository.findAllByPartyId(partyId)).thenReturn(List.of());

        List<PartyLotView> lots = newService().listLots(new ListLotsByPartyQuery(partyId));

        assertThat(lots).isEmpty();
    }
}
