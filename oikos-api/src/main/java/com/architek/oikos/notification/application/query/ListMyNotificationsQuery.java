package com.architek.oikos.notification.application.query;

import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ListMyNotificationsQuery(EntityId userId, PageRequest pageRequest) {
}
