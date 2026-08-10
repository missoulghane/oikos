package com.architek.oikos.messaging.web.response;

import java.time.Instant;

import com.architek.oikos.messaging.application.dto.MessageView;

public record MessageResponse(String id, String conversationId, String senderId, String senderName, String body,
                               Instant createdAt, boolean mine) {

    public static MessageResponse from(MessageView view) {
        return new MessageResponse(view.id().toString(), view.conversationId().toString(), view.senderId().toString(),
                view.senderName(), view.body(), view.createdAt(), view.mine());
    }
}
