package com.architek.oikos.notification.application.command;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record RegisterDevicePushTokenCommand(EntityId userId, String expoPushToken) {
}
