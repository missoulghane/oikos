package com.architek.oikos.user.application.port.out;

import java.time.Instant;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record OwnedMembershipRequestView(EntityId id, String propertyName, String unitNumber, String unitTypeName,
                                          String status, Instant decidedAt, String rejectionReason) {
}
