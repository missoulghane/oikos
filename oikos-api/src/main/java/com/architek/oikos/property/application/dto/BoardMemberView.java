package com.architek.oikos.property.application.dto;

import com.architek.oikos.property.domain.model.BoardMember;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record BoardMemberView(BoardMemberId id, PropertyId propertyId, EntityId contactId, BoardRole boardRole) {

    public static BoardMemberView from(BoardMember boardMember) {
        return new BoardMemberView(boardMember.getId(), boardMember.getPropertyId(), boardMember.getContactId(),
                boardMember.getBoardRole());
    }
}
