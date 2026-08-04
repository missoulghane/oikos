package com.architek.oikos.invitation.domain.exception;

import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class MembershipRequestNotFoundException extends ResourceNotFoundException {

    public MembershipRequestNotFoundException(MembershipRequestId id) {
        super("Membership request not found with id: " + id);
    }
}
