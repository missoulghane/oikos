package com.architek.oikos.messaging.application.query;

import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;

public record GetMessageDraftQuery(MessageDraftId draftId) {
}
