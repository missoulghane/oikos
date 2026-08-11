package com.architek.oikos.messaging.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** No recipientUserIds (membership is resolved dynamically from the property's current staff
 * roster, see Conversation's javadoc) and no senderIdentity (always BOARD, no other identity a
 * private board thread could be posted under). */
public record StartBoardConversationRequest(@NotBlank @Size(max = 200) String subject,
                                             @NotBlank @Size(max = 4000) String body) {
}
