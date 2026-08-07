package com.architek.oikos.property.application.dto;

import com.architek.oikos.property.domain.model.BoardMember;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;
import com.architek.oikos.property.domain.valueobject.BoardMemberStatus;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record BoardMemberView(BoardMemberId id, PropertyId propertyId, EntityId partyId, BoardRole boardRole,
                               String partyFullName, String partyEmail, boolean hasLinkedAccount,
                               BoardMemberStatus status) {

    public static BoardMemberView from(BoardMember boardMember, String partyFullName, String partyEmail,
                                        boolean hasLinkedAccount) {
        return new BoardMemberView(boardMember.getId(), boardMember.getPropertyId(), boardMember.getPartyId(),
                boardMember.getBoardRole(), partyFullName, partyEmail, hasLinkedAccount, boardMember.getStatus());
    }
}
