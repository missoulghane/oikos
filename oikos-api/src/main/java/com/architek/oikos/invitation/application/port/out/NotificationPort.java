package com.architek.oikos.invitation.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Narrowly named after the one notification event invitation currently
 * produces (REQUEST_RECEIVED) rather than a generic `create(type, ...)` -
 * add a sibling method here, not a type parameter, if a second producer
 * event is ever needed from this module.
 */
public interface NotificationPort {

    void notifyRequestReceived(EntityId recipientUserId, EntityId propertyId, String title, String body, String linkPath);
}
