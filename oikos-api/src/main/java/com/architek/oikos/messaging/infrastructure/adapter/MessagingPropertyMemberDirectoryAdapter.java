package com.architek.oikos.messaging.infrastructure.adapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.architek.oikos.messaging.application.port.out.PropertyMemberDirectoryPort;
import com.architek.oikos.messaging.application.port.out.PropertyMemberInfo;
import com.architek.oikos.property.application.dto.BoardMemberView;
import com.architek.oikos.property.application.dto.PropertyContactView;
import com.architek.oikos.property.application.dto.PropertyView;
import com.architek.oikos.property.application.port.in.GetPropertyUseCase;
import com.architek.oikos.property.application.port.in.ListBoardMembersByPropertyUseCase;
import com.architek.oikos.property.application.port.in.ListContactsByPropertyUseCase;
import com.architek.oikos.property.application.query.GetPropertyQuery;
import com.architek.oikos.property.application.query.ListBoardMembersByPropertyQuery;
import com.architek.oikos.property.application.query.ListContactsByPropertyQuery;
import com.architek.oikos.property.domain.valueobject.BoardMemberStatus;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: delegates exclusively to property's public port-in
 * use cases (GetPropertyUseCase, ListContactsByPropertyUseCase already
 * exposing hasLinkedAccount, ListBoardMembersByPropertyUseCase filtered to
 * ACTIVE seats), never to property's domain model or repositories directly
 * (rule 6) - zero change made in the property module itself. A property's
 * member count stays small enough to hold in memory (same assumption as
 * ListContactsByPropertyService's own internal building/unit walk), so
 * contacts are paged through in full here rather than exposing pagination on
 * this port.
 */
@Component
public class MessagingPropertyMemberDirectoryAdapter implements PropertyMemberDirectoryPort {

    private static final int PAGE_SIZE = 100;
    private static final String OWNER_ROLE_LABEL = "Copropriétaire";
    private static final String BOARD_ROLE_LABEL = "Bureau de syndic";

    private final GetPropertyUseCase getPropertyUseCase;
    private final ListContactsByPropertyUseCase listContactsByPropertyUseCase;
    private final ListBoardMembersByPropertyUseCase listBoardMembersByPropertyUseCase;

    public MessagingPropertyMemberDirectoryAdapter(GetPropertyUseCase getPropertyUseCase,
                                                     ListContactsByPropertyUseCase listContactsByPropertyUseCase,
                                                     ListBoardMembersByPropertyUseCase listBoardMembersByPropertyUseCase) {
        this.getPropertyUseCase = getPropertyUseCase;
        this.listContactsByPropertyUseCase = listContactsByPropertyUseCase;
        this.listBoardMembersByPropertyUseCase = listBoardMembersByPropertyUseCase;
    }

    @Override
    public String getPropertyName(EntityId propertyId) {
        PropertyView view = getPropertyUseCase.getProperty(new GetPropertyQuery(PropertyId.of(propertyId.value())));
        return view.name();
    }

    @Override
    public List<PropertyMemberInfo> listMembers(EntityId propertyId) {
        PropertyId typedPropertyId = PropertyId.of(propertyId.value());
        // Keyed by partyId: ListContactsByPropertyUseCase flattens one row per unit-ownership, so
        // a party owning several units would otherwise appear more than once.
        Map<EntityId, PropertyMemberInfo> members = new LinkedHashMap<>();

        for (PropertyContactView contact : listAllContacts(typedPropertyId)) {
            members.putIfAbsent(contact.partyId(),
                    new PropertyMemberInfo(contact.partyId(), contact.partyFullName(), OWNER_ROLE_LABEL, contact.hasLinkedAccount()));
        }

        for (BoardMemberView boardMember : listBoardMembersByPropertyUseCase.listBoardMembers(new ListBoardMembersByPropertyQuery(typedPropertyId))) {
            if (boardMember.status() == BoardMemberStatus.ACTIVE) {
                members.put(boardMember.partyId(), new PropertyMemberInfo(boardMember.partyId(), boardMember.partyFullName(),
                        BOARD_ROLE_LABEL, boardMember.hasLinkedAccount()));
            }
        }

        return new ArrayList<>(members.values());
    }

    private List<PropertyContactView> listAllContacts(PropertyId propertyId) {
        List<PropertyContactView> contacts = new ArrayList<>();
        int pageNumber = 0;
        Page<PropertyContactView> page;
        do {
            page = listContactsByPropertyUseCase.listContacts(
                    new ListContactsByPropertyQuery(propertyId, PageRequest.of(pageNumber, PAGE_SIZE), null));
            contacts.addAll(page.content());
            pageNumber++;
        } while (page.hasNext());
        return contacts;
    }
}
