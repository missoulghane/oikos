package com.architek.oikos.notification.domain.repository;

import java.util.List;

import com.architek.oikos.notification.domain.model.DevicePushToken;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface DevicePushTokenRepository {

    /** Upserts on the token itself (see DevicePushTokenEntity's unique constraint), not on id - see DevicePushToken's javadoc. */
    DevicePushToken save(DevicePushToken devicePushToken);

    List<String> findTokensByUserId(EntityId userId);
}
