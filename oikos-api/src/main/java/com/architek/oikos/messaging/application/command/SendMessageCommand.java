package com.architek.oikos.messaging.application.command;

import com.architek.oikos.messaging.domain.model.SenderIdentity;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageBody;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** senderIdentity is the client's claim - only honored for a GROUP conversation, and only once
 * verified the sender actually holds that role on the conversation's property (see
 * SendMessageService); ignored (forced BOARD) for BOARD_PRIVATE/BROADCAST. */
public record SendMessageCommand(ConversationId conversationId, EntityId senderId, MessageBody body,
                                  SenderIdentity senderIdentity) {
}
