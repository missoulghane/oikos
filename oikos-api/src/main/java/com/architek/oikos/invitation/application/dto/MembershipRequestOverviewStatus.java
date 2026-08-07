package com.architek.oikos.invitation.application.dto;

/**
 * Application-layer status for the manager's unified membership-request
 * overview - distinct from the persisted domain enum MembershipRequestStatus
 * (PENDING/ACCEPTED/REJECTED only). INVITED is synthetic and never stored:
 * it represents a still-outstanding PRIVATE invitation nobody has accepted
 * yet (see MembershipRequestOverviewView.fromInvitation), derived at read
 * time rather than persisted, since a PRIVATE invitation has no unit/party/
 * user to attach a real MembershipRequest row to until it's actually accepted.
 */
public enum MembershipRequestOverviewStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    INVITED
}
