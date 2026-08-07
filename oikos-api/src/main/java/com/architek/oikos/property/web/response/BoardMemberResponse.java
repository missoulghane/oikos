package com.architek.oikos.property.web.response;

import com.architek.oikos.property.application.dto.BoardMemberView;
import com.architek.oikos.property.domain.valueobject.BoardMemberStatus;

public record BoardMemberResponse(String id, String propertyId, String partyId, String boardRole,
                                   String partyFullName, String partyEmail, boolean hasLinkedAccount,
                                   BoardMemberStatus status) {

    public static BoardMemberResponse from(BoardMemberView view) {
        return new BoardMemberResponse(view.id().toString(), view.propertyId().toString(), view.partyId().toString(),
                view.boardRole().name(), view.partyFullName(), view.partyEmail(), view.hasLinkedAccount(),
                view.status());
    }
}
