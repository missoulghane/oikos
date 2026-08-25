package com.architek.oikos.notification.application.query;

import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * unreadOnly restreint aux notifications jamais ouvertes - ce que liste la
 * cloche du header, pour qu'elle montre exactement ce que compte sa pastille.
 */
public record ListMyNotificationsQuery(EntityId userId, boolean unreadOnly, PageRequest pageRequest) {

    public ListMyNotificationsQuery(EntityId userId, PageRequest pageRequest) {
        this(userId, false, pageRequest);
    }
}
