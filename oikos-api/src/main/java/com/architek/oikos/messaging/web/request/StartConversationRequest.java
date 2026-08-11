package com.architek.oikos.messaging.web.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import com.architek.oikos.messaging.domain.model.SenderIdentity;

/** senderIdentity may be omitted (null) when the sender is only eligible for one identity on the
 * property - required only when they hold both (see SenderIdentityValidator.resolve). concernsUnit
 * is an optional free-text lot label (see Conversation's javadoc) - never validated against the
 * property's actual unit registry, purely a display hint. */
public record StartConversationRequest(@NotEmpty List<String> recipientUserIds,
                                        @NotBlank @Size(max = 200) String subject,
                                        @NotBlank @Size(max = 4000) String body,
                                        SenderIdentity senderIdentity,
                                        @Size(max = 100) String concernsUnit) {
}
