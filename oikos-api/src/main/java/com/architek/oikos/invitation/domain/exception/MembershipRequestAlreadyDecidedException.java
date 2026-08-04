package com.architek.oikos.invitation.domain.exception;

import com.architek.oikos.shared.exception.ConflictException;

public class MembershipRequestAlreadyDecidedException extends ConflictException {

    public MembershipRequestAlreadyDecidedException() {
        super("This membership request has already been decided");
    }
}
