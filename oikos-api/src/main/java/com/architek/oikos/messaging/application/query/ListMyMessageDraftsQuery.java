package com.architek.oikos.messaging.application.query;

import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** search filters by subject/body substring, case-insensitive; null means no filter. */
public record ListMyMessageDraftsQuery(EntityId userId, PageRequest pageRequest, String search) {
}
