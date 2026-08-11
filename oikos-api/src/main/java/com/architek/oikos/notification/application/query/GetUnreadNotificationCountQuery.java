package com.architek.oikos.notification.application.query;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record GetUnreadNotificationCountQuery(EntityId userId) {
}
