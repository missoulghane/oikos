package com.architek.oikos.messaging.application.dto;

import java.time.Instant;
import java.util.List;

import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * recipients lists every currently-resolvable recipient's display name
 * (same MemberDisplayNameResolver ConversationAggregator already uses) -
 * always empty when broadcast is true, mirroring ConversationSummaryView's
 * participants being empty for BROADCAST.
 */
public record MessageDraftSummaryView(MessageDraftId id, EntityId propertyId, String propertyName, boolean broadcast,
                                       List<ConversationParticipantView> recipients, String subject, String body,
                                       Instant lastModifiedAt) {
}
