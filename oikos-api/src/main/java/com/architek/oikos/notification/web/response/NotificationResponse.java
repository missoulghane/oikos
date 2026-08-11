package com.architek.oikos.notification.web.response;

import java.time.Instant;

import com.architek.oikos.notification.application.dto.NotificationView;
import com.architek.oikos.notification.domain.model.NotificationType;

public record NotificationResponse(String id, String propertyId, NotificationType type, String title, String body,
                                    String linkPath, boolean read, Instant createdAt) {

    public static NotificationResponse from(NotificationView view) {
        return new NotificationResponse(view.id().toString(), view.propertyId() != null ? view.propertyId().toString() : null,
                view.type(), view.title(), view.body(), view.linkPath(), view.read(), view.createdAt());
    }
}
