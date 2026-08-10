package com.architek.oikos.messaging.web.response;

import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;

public record MessageDraftReferenceResponse(String draftId) {

    public static MessageDraftReferenceResponse from(MessageDraftId id) {
        return new MessageDraftReferenceResponse(id.toString());
    }
}
