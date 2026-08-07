package com.architek.oikos.user.web.response;

import java.time.Instant;

import com.architek.oikos.user.application.port.out.OwnedMembershipRequestView;

public record OwnedMembershipRequestResponse(String id, String propertyName, String unitNumber, String unitTypeName,
                                              String status, Instant decidedAt, String rejectionReason) {

    public static OwnedMembershipRequestResponse from(OwnedMembershipRequestView view) {
        return new OwnedMembershipRequestResponse(view.id().toString(), view.propertyName(), view.unitNumber(),
                view.unitTypeName(), view.status(), view.decidedAt(), view.rejectionReason());
    }
}
