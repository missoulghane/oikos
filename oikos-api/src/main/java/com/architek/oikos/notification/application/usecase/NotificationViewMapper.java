package com.architek.oikos.notification.application.usecase;

import com.architek.oikos.notification.application.dto.NotificationView;
import com.architek.oikos.notification.domain.model.Notification;

/** Shared by ListMyNotificationsService and GetNotificationService - both expose the exact same
 * shape (see NotificationView's javadoc), so the one mapping lives here instead of twice. */
final class NotificationViewMapper {

    private NotificationViewMapper() {
    }

    static NotificationView toView(Notification notification) {
        return new NotificationView(notification.getId(), notification.getRecipientUserId(), notification.getPropertyId(),
                notification.getType(), notification.getTitle(), notification.getBody(), notification.getLinkPath(),
                notification.isRead(), notification.getCreatedDate());
    }
}
