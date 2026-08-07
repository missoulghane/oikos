package com.architek.oikos.invitation.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.invitation.application.port.out.BoardDirectoryPort;
import com.architek.oikos.property.application.command.CreatePendingBoardMemberCommand;
import com.architek.oikos.property.application.port.in.CreatePendingBoardMemberUseCase;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: delegates to property's public port-in
 * (CreatePendingBoardMemberUseCase), never to property's repository directly
 * (rule 6). The only place invitation's stored targetBoardRole String is
 * resolved back into property's own BoardRole enum, right before calling
 * into property's port. The party already exists at this point (resolved in
 * AcceptInvitationService). The resulting seat starts PENDING_VALIDATION -
 * accepting the invitation link no longer grants the board role by itself.
 */
@Component
public class InvitationBoardDirectoryAdapter implements BoardDirectoryPort {

    private final CreatePendingBoardMemberUseCase createPendingBoardMemberUseCase;

    public InvitationBoardDirectoryAdapter(CreatePendingBoardMemberUseCase createPendingBoardMemberUseCase) {
        this.createPendingBoardMemberUseCase = createPendingBoardMemberUseCase;
    }

    @Override
    public void addPendingBoardMember(EntityId propertyId, EntityId partyId, EntityId userId, String boardRole) {
        createPendingBoardMemberUseCase.create(new CreatePendingBoardMemberCommand(
                PropertyId.of(propertyId.value()), partyId, userId, BoardRole.valueOf(boardRole)));
    }
}
