package com.architek.oikos.messaging.web.response;

import com.architek.oikos.messaging.domain.valueobject.ConversationId;

/** Minimal "reference" response shared by both start-conversation and send-broadcast endpoints - same pattern as accounting's JournalEntryReferenceResponse. */
public record ConversationReferenceResponse(String conversationId) {

    public static ConversationReferenceResponse from(ConversationId id) {
        return new ConversationReferenceResponse(id.toString());
    }
}
