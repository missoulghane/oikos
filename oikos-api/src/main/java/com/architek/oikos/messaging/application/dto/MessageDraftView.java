package com.architek.oikos.messaging.application.dto;

import java.util.List;

import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Full projection of a draft - used both to prefill the edit form (GET one,
 * recipients resolved with display names so the frontend can render chips
 * without a second round trip) and, via GetMessageDraftUseCase, by
 * PropertyAccessEvaluator.isDraftOwner (rule 6: cross-feature access only
 * through a port-in view, same pattern as ConversationView/
 * isConversationParticipant - isDraftOwner itself only reads id/createdBy).
 */
public record MessageDraftView(MessageDraftId id, EntityId propertyId, EntityId createdBy,
                                List<ConversationParticipantView> recipients, boolean broadcast, String subject,
                                String body) {
}
