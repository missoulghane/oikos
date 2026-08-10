package com.architek.oikos.messaging.application.query;

import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** search filters by any other participant's name (GROUP) or property name (BROADCAST), case-insensitive substring; null means no filter. */
public record ListMyConversationsQuery(EntityId userId, PageRequest pageRequest, String search) {
}
