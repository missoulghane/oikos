package com.architek.oikos.messaging.web.response;

import java.util.List;

import com.architek.oikos.messaging.application.dto.MessageDraftView;

/** Full projection of a draft, used to prefill the "Nouveau message" compose form when resuming
 * an existing draft (GET /message-drafts/{id}) - recipients carry resolved display names (same
 * shape as ConversationSummaryResponse's participants) so the recipient picker can render chips
 * without a second round trip. */
public record MessageDraftResponse(String id, String propertyId, boolean broadcast,
                                    List<ConversationParticipantResponse> recipients, String subject, String body) {

    public static MessageDraftResponse from(MessageDraftView view) {
        return new MessageDraftResponse(view.id().toString(), view.propertyId().toString(), view.broadcast(),
                view.recipients().stream().map(ConversationParticipantResponse::from).toList(), view.subject(), view.body());
    }
}
