package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.dto.PropertyContactView;
import com.architek.oikos.property.application.port.out.PartyDetails;
import com.architek.oikos.property.application.port.out.PartyDirectoryPort;
import com.architek.oikos.property.application.query.ListContactsByPropertyQuery;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
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
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;

@ExtendWith(MockitoExtension.class)
class ListContactsByPropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private UnitOwnershipRepository unitOwnershipRepository;

    @Mock
    private PartyDirectoryPort partyDirectoryPort;

    private ListContactsByPropertyService newService() {
        return new ListContactsByPropertyService(propertyRepository, buildingRepository, unitRepository,
                unitOwnershipRepository, partyDirectoryPort);
    }

    @Test
    void listing_contacts_of_a_missing_property_throws() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().listContacts(new ListContactsByPropertyQuery(propertyId)))
                .isInstanceOf(PropertyNotFoundException.class);
    }

    @Test
    void listing_contacts_walks_every_building_and_unit_then_enriches_ownerships_with_party_and_lot_details() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId)).thenReturn(
                Optional.of(Property.create(propertyId, "Copro Test", "1 rue de la Paix")));

        BuildingId buildingId = BuildingId.newId();
        Building building = Building.create(buildingId, propertyId, "Bâtiment A", 3);
        when(buildingRepository.findAllByPropertyId(propertyId, PageRequest.of(0, 100)))
                .thenReturn(Page.of(List.of(building), 0, 100, 1));

        UnitId unitId = UnitId.newId();
        Unit unit = Unit.create(unitId, buildingId, propertyId, "A12", UnitTypeDefinitionId.newId(),
                Shares.of(new BigDecimal("150")));
        when(unitRepository.findAllByBuildingId(buildingId, PageRequest.of(0, 100)))
                .thenReturn(Page.of(List.of(unit), 0, 100, 1));

        EntityId partyId = EntityId.newId();
        UnitOwnership unitOwnership = UnitOwnership.create(UnitOwnershipId.newId(), unitId, partyId, propertyId,
                OwnershipShare.of(new BigDecimal("50")));
        when(unitOwnershipRepository.findAllByUnitIds(anyList())).thenReturn(List.of(unitOwnership));
        when(partyDirectoryPort.getPartyById(any()))
                .thenReturn(new PartyDetails("Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane.doe@example.com"), null));

        List<PropertyContactView> contacts = newService().listContacts(new ListContactsByPropertyQuery(propertyId));

        assertThat(contacts).hasSize(1);
        PropertyContactView contact = contacts.get(0);
        assertThat(contact.partyFullName()).isEqualTo("Jane Doe");
        assertThat(contact.unitNumber()).isEqualTo("A12");
        assertThat(contact.buildingName()).isEqualTo("Bâtiment A");
        assertThat(contact.ownershipShare()).isEqualByComparingTo("50");
    }
}
