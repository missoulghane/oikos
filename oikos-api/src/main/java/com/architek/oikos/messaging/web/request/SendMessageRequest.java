package com.architek.oikos.messaging.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.architek.oikos.messaging.domain.model.SenderIdentity;

/** Shared by both POST /conversations/{id}/messages and POST /properties/{id}/broadcast-messages - same shape
 * either way. senderIdentity is read only by the former (a GROUP reply may need it, see
 * SenderIdentityValidator.resolve); the broadcast endpoint's handler never looks at it, since a
 * broadcast message is always posted as BOARD regardless. */
public record SendMessageRequest(@NotBlank @Size(max = 4000) String body, SenderIdentity senderIdentity) {
}
