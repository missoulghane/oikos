package com.architek.oikos.notification.domain.model;

import java.util.Objects;

import com.architek.oikos.notification.domain.valueobject.DevicePushTokenId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A single (user, Expo push token) registration - one row per device the
 * mobile app has been granted push permission on and registered after
 * login, so CreateNotificationService can also deliver a push alongside the
 * in-app Notification row it creates. No state transitions: re-registering
 * the same token (app reopened) is an upsert at the repository level keyed
 * on the token itself (see DevicePushTokenRepository.save), not a
 * domain-level update - a token that moved to a different account (device
 * passed on) migrates rather than duplicating.
 */
public record DevicePushToken(DevicePushTokenId id, EntityId userId, String expoPushToken) {

    public DevicePushToken {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        if (expoPushToken == null || expoPushToken.isBlank()) {
            throw new IllegalArgumentException("expoPushToken must not be blank");
        }
    }

    public static DevicePushToken register(EntityId userId, String expoPushToken) {
        return new DevicePushToken(DevicePushTokenId.newId(), userId, expoPushToken);
    }
}
