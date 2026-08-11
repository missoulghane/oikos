package com.architek.oikos.notification.application.command;

import com.architek.oikos.notification.domain.model.NotificationType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Not called by anything yet (see Notification's javadoc) - this is the
 * entry point a future producer in another module reaches through
 * CreateNotificationUseCase once a concrete triggering event is wired up.
 */
public record CreateNotificationCommand(EntityId recipientUserId, EntityId propertyId, NotificationType type, String title,
                                         String body, String linkPath) {
}
