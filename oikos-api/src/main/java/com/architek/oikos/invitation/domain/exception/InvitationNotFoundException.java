package com.architek.oikos.invitation.domain.exception;

import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class InvitationNotFoundException extends ResourceNotFoundException {

    public InvitationNotFoundException(InvitationId id) {
        super("Invitation not found with id: " + id);
    }
}
