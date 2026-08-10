package com.architek.oikos.messaging.application.command;

import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;

public record DeleteMessageDraftCommand(MessageDraftId draftId) {
}
