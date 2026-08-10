package com.architek.oikos.messaging.application.dto;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/** One participant of a GROUP conversation, other than the caller - see ConversationSummaryView. */
public record ConversationParticipantView(EntityId userId, String fullName) {
}
