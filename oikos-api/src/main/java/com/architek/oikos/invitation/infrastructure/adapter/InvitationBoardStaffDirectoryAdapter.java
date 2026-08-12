package com.architek.oikos.invitation.infrastructure.adapter;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.architek.oikos.invitation.application.port.out.BoardStaffDirectoryPort;
import com.architek.oikos.property.application.dto.BoardMemberView;
import com.architek.oikos.property.application.port.in.ListBoardMembersByPropertyUseCase;
import com.architek.oikos.property.application.query.ListBoardMembersByPropertyQuery;
import com.architek.oikos.property.domain.valueobject.BoardMemberStatus;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.port.in.FindUsersByPartyIdsUseCase;

/**
 * Cross-feature adapter: delegates exclusively to property's and user's
 * public port-in use cases (ListBoardMembersByPropertyUseCase filtered to
 * ACTIVE seats, FindUsersByPartyIdsUseCase to turn a party into a platform
 * account), never to their repositories directly (rule 6) - mirrors
 * messaging's MessagingPropertyMemberDirectoryAdapter / MessagingPartyAccountDirectoryAdapter
 * board-loop exactly.
 */
@Component
public class InvitationBoardStaffDirectoryAdapter implements BoardStaffDirectoryPort {

    private final ListBoardMembersByPropertyUseCase listBoardMembersByPropertyUseCase;
    private final FindUsersByPartyIdsUseCase findUsersByPartyIdsUseCase;

    public InvitationBoardStaffDirectoryAdapter(ListBoardMembersByPropertyUseCase listBoardMembersByPropertyUseCase,
                                                 FindUsersByPartyIdsUseCase findUsersByPartyIdsUseCase) {
        this.listBoardMembersByPropertyUseCase = listBoardMembersByPropertyUseCase;
        this.findUsersByPartyIdsUseCase = findUsersByPartyIdsUseCase;
    }

    @Override
    public List<EntityId> listStaffUserIds(EntityId propertyId) {
        PropertyId typedPropertyId = PropertyId.of(propertyId.value());
        List<EntityId> activeBoardPartyIds = listBoardMembersByPropertyUseCase
                .listBoardMembers(new ListBoardMembersByPropertyQuery(typedPropertyId)).stream()
                .filter(member -> member.status() == BoardMemberStatus.ACTIVE)
                .map(BoardMemberView::partyId)
                .toList();
        Map<EntityId, EntityId> userIdsByPartyId = findUsersByPartyIdsUseCase.findUserIdsByPartyIds(activeBoardPartyIds);
        return List.copyOf(userIdsByPartyId.values());
    }
}
