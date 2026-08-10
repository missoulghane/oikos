package com.architek.oikos.messaging.web.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record StartConversationRequest(@NotEmpty List<String> recipientUserIds,
                                        @NotBlank @Size(max = 200) String subject,
                                        @NotBlank @Size(max = 4000) String body) {
}
