package com.architek.oikos.messaging.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Shared by both POST /conversations/{id}/messages and POST /properties/{id}/broadcast-messages - same {body} shape either way. */
public record SendMessageRequest(@NotBlank @Size(max = 4000) String body) {
}
