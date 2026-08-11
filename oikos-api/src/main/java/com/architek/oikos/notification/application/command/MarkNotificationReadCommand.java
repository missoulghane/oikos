package com.architek.oikos.notification.application.command;

import com.architek.oikos.notification.domain.valueobject.NotificationId;

public record MarkNotificationReadCommand(NotificationId id) {
}
