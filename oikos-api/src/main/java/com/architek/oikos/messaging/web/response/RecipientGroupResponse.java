package com.architek.oikos.messaging.web.response;

import java.util.List;

import com.architek.oikos.messaging.application.dto.RecipientGroupView;

public record RecipientGroupResponse(String id, String propertyId, String name,
                                       List<ConversationParticipantResponse> members) {

    public static RecipientGroupResponse from(RecipientGroupView view) {
        return new RecipientGroupResponse(view.id().toString(), view.propertyId().toString(), view.name(),
                view.members().stream().map(ConversationParticipantResponse::from).toList());
    }
}
