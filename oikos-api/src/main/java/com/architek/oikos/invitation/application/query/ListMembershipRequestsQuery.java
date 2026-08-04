package com.architek.oikos.invitation.application.query;

import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ListMembershipRequestsQuery(EntityId propertyId, PageRequest pageRequest) {
}
