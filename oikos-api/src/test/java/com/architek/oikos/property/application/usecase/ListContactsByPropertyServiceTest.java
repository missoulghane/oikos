package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.dto.PropertyContactView;
import com.architek.oikos.property.application.port.out.AccountLinkingPort;
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

    @Mock
    private AccountLinkingPort accountLinkingPort;

    private ListContactsByPropertyService newService() {
        return new ListContactsByPropertyService(propertyRepository, buildingRepository, unitRepository,
                unitOwnershipRepository, partyDirectoryPort, accountLinkingPort);
    }

    @Test
    void listing_contacts_of_a_missing_property_throws() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService()
                .listContacts(new ListContactsByPropertyQuery(propertyId, PageRequest.of(0, 20), null)))
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
        when(accountLinkingPort.findLinkedPartyIds(anyList())).thenReturn(Set.of(partyId));

        List<PropertyContactView> contacts = newService()
                .listContacts(new ListContactsByPropertyQuery(propertyId, PageRequest.of(0, 20), null)).content();

        assertThat(contacts).hasSize(1);
        PropertyContactView contact = contacts.get(0);
        assertThat(contact.partyFullName()).isEqualTo("Jane Doe");
        assertThat(contact.unitNumber()).isEqualTo("A12");
        assertThat(contact.buildingName()).isEqualTo("Bâtiment A");
        assertThat(contact.ownershipShare()).isEqualByComparingTo("50");
        assertThat(contact.hasLinkedAccount()).isTrue();
    }

    @Test
    void listing_contacts_reports_no_linked_account_when_the_party_id_is_absent_from_the_linked_set() {
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
        when(accountLinkingPort.findLinkedPartyIds(anyList())).thenReturn(Set.of());

        List<PropertyContactView> contacts = newService()
                .listContacts(new ListContactsByPropertyQuery(propertyId, PageRequest.of(0, 20), null)).content();

        assertThat(contacts).hasSize(1);
        assertThat(contacts.get(0).hasLinkedAccount()).isFalse();
    }

    @Test
    void searching_by_phone_keeps_only_the_matching_party_s_units_and_paginates_by_distinct_party() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId)).thenReturn(
                Optional.of(Property.create(propertyId, "Copro Test", "1 rue de la Paix")));

        BuildingId buildingId = BuildingId.newId();
        Building building = Building.create(buildingId, propertyId, "Bâtiment A", 3);
        when(buildingRepository.findAllByPropertyId(propertyId, PageRequest.of(0, 100)))
                .thenReturn(Page.of(List.of(building), 0, 100, 1));

        UnitId unitId1 = UnitId.newId();
        UnitId unitId2 = UnitId.newId();
        Unit unit1 = Unit.create(unitId1, buildingId, propertyId, "A12", UnitTypeDefinitionId.newId(),
                Shares.of(new BigDecimal("150")));
        Unit unit2 = Unit.create(unitId2, buildingId, propertyId, "A13", UnitTypeDefinitionId.newId(),
                Shares.of(new BigDecimal("100")));
        when(unitRepository.findAllByBuildingId(buildingId, PageRequest.of(0, 100)))
                .thenReturn(Page.of(List.of(unit1, unit2), 0, 100, 2));

        EntityId matchingPartyId = EntityId.newId();
        EntityId otherPartyId = EntityId.newId();
        UnitOwnership matchingOwnership = UnitOwnership.create(UnitOwnershipId.newId(), unitId1, matchingPartyId, propertyId,
                OwnershipShare.of(new BigDecimal("50")));
        UnitOwnership otherOwnership = UnitOwnership.create(UnitOwnershipId.newId(), unitId2, otherPartyId, propertyId,
                OwnershipShare.of(new BigDecimal("50")));
        when(unitOwnershipRepository.findAllByUnitIds(anyList())).thenReturn(List.of(matchingOwnership, otherOwnership));
        when(partyDirectoryPort.getPartyById(matchingPartyId)).thenReturn(
                new PartyDetails("Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane.doe@example.com"), "0601020304"));
        when(partyDirectoryPort.getPartyById(otherPartyId)).thenReturn(
                new PartyDetails("John Smith", PartyType.INDIVIDUAL, EmailVO.of("john.smith@example.com"), "0699999999"));
        when(accountLinkingPort.findLinkedPartyIds(anyList())).thenReturn(Set.of());

        var page = newService().listContacts(new ListContactsByPropertyQuery(propertyId, PageRequest.of(0, 20), "0102"));

        assertThat(page.content()).extracting(PropertyContactView::partyFullName).containsExactly("Jane Doe");
        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    void filtering_on_the_account_status_keeps_only_the_contacts_without_a_linked_account() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId)).thenReturn(
                Optional.of(Property.create(propertyId, "Copro Test", "1 rue de la Paix")));

        BuildingId buildingId = BuildingId.newId();
        Building building = Building.create(buildingId, propertyId, "Bâtiment A", 3);
        when(buildingRepository.findAllByPropertyId(propertyId, PageRequest.of(0, 100)))
                .thenReturn(Page.of(List.of(building), 0, 100, 1));

        UnitId unitId1 = UnitId.newId();
        UnitId unitId2 = UnitId.newId();
        Unit unit1 = Unit.create(unitId1, buildingId, propertyId, "A12", UnitTypeDefinitionId.newId(),
                Shares.of(new BigDecimal("150")));
        Unit unit2 = Unit.create(unitId2, buildingId, propertyId, "A13", UnitTypeDefinitionId.newId(),
                Shares.of(new BigDecimal("100")));
        when(unitRepository.findAllByBuildingId(buildingId, PageRequest.of(0, 100)))
                .thenReturn(Page.of(List.of(unit1, unit2), 0, 100, 2));

        EntityId linkedPartyId = EntityId.newId();
        EntityId unlinkedPartyId = EntityId.newId();
        when(unitOwnershipRepository.findAllByUnitIds(anyList())).thenReturn(List.of(
                UnitOwnership.create(UnitOwnershipId.newId(), unitId1, linkedPartyId, propertyId,
                        OwnershipShare.of(new BigDecimal("50"))),
                UnitOwnership.create(UnitOwnershipId.newId(), unitId2, unlinkedPartyId, propertyId,
                        OwnershipShare.of(new BigDecimal("50")))));
        when(partyDirectoryPort.getPartyById(linkedPartyId)).thenReturn(
                new PartyDetails("Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane.doe@example.com"), null));
        when(partyDirectoryPort.getPartyById(unlinkedPartyId)).thenReturn(
                new PartyDetails("John Smith", PartyType.INDIVIDUAL, EmailVO.of("john.smith@example.com"), null));
        when(accountLinkingPort.findLinkedPartyIds(anyList())).thenReturn(Set.of(linkedPartyId));

        var withoutAccount = newService()
                .listContacts(new ListContactsByPropertyQuery(propertyId, PageRequest.of(0, 20), null, false));

        assertThat(withoutAccount.content()).extracting(PropertyContactView::partyFullName).containsExactly("John Smith");
        assertThat(withoutAccount.totalElements()).isEqualTo(1);

        var withAccount = newService()
                .listContacts(new ListContactsByPropertyQuery(propertyId, PageRequest.of(0, 20), null, true));

        assertThat(withAccount.content()).extracting(PropertyContactView::partyFullName).containsExactly("Jane Doe");
        assertThat(withAccount.totalElements()).isEqualTo(1);
    }
}
