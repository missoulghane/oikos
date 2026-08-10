package com.architek.oikos.messaging.web.response;

import com.architek.oikos.messaging.application.dto.ConversationParticipantView;

public record ConversationParticipantResponse(String userId, String fullName) {

    public static ConversationParticipantResponse from(ConversationParticipantView view) {
        return new ConversationParticipantResponse(view.userId().toString(), view.fullName());
    }
}
