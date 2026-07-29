package com.architek.oikos.user.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

public class InvalidPartyInvitationTokenException extends BusinessException {

    public InvalidPartyInvitationTokenException(String message) {
        super(message);
    }
}
