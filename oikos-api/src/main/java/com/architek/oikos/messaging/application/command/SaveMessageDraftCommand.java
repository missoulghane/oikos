package com.architek.oikos.messaging.application.command;

import java.util.Set;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/** Shared payload for both create and update - a draft may be genuinely incomplete, so unlike
 * StartGroupConversationCommand/SendBroadcastMessageCommand, subject/body are raw nullable
 * Strings here, not ConversationSubject/MessageBody value objects (those validate non-blank,
 * which a draft must not be forced into until it's actually sent - see SendMessageDraftService). */
public record SaveMessageDraftCommand(Set<EntityId> recipientUserIds, boolean broadcast, String subject, String body) {
}
