package com.architek.oikos.notification.application.dto;

import java.time.Instant;

import com.architek.oikos.notification.domain.model.NotificationType;
import com.architek.oikos.notification.domain.valueobject.NotificationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Same shape for both the paginated inbox (ListMyNotificationsUseCase) and
 * the single-resource lookup (GetNotificationUseCase) - unlike messaging's
 * ConversationSummaryView/ConversationView split, there is no cross-source
 * aggregation or per-caller computed field (unreadCount, participants...)
 * that would make the two views actually diverge here, so one record covers
 * both rather than introducing a second one that would only ever mirror it.
 */
public record NotificationView(NotificationId id, EntityId recipientUserId, EntityId propertyId, NotificationType type,
                                String title, String body, String linkPath, boolean read, Instant createdAt) {
}
