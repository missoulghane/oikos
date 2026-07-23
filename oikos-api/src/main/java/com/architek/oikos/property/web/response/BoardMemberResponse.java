package com.architek.oikos.property.web.response;

import com.architek.oikos.property.application.dto.BoardMemberView;

public record BoardMemberResponse(String id, String propertyId, String partyId, String boardRole) {

    public static BoardMemberResponse from(BoardMemberView view) {
        return new BoardMemberResponse(view.id().toString(), view.propertyId().toString(), view.partyId().toString(),
                view.boardRole().name());
    }
}
