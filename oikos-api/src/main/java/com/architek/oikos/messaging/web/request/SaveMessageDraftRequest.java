package com.architek.oikos.messaging.web.request;

import java.util.List;

import jakarta.validation.constraints.Size;

/** Backs both create and update - deliberately no @NotEmpty/@NotBlank, unlike
 * StartConversationRequest/SendMessageRequest: a draft is allowed to be incomplete while being
 * composed (see MessageDraft's javadoc), real validation only happens at send time. */
public record SaveMessageDraftRequest(List<String> recipientUserIds, boolean broadcast,
                                       @Size(max = 200) String subject, @Size(max = 4000) String body) {
}
