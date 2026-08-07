package com.architek.oikos.invitation.application.dto;

import java.time.Instant;

import com.architek.oikos.invitation.domain.model.MembershipRequestStatus;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;

/**
 * Enriched, self-service view of a MembershipRequest - unlike
 * MembershipRequestView (the manager-facing read model, raw ids only), this
 * resolves the property/unit names so the requester's own "my membership
 * requests" page has something readable to show.
 */
public record MembershipRequestSummaryView(MembershipRequestId id, String propertyName, String unitNumber,
                                            String unitTypeName, MembershipRequestStatus status, Instant decidedAt,
                                            String rejectionReason) {
}
