package com.architek.oikos.messaging.application.query;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record GetUnreadSummaryQuery(EntityId userId) {
}
