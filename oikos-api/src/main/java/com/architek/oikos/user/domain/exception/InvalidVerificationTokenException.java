package com.architek.oikos.user.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

public class InvalidVerificationTokenException extends BusinessException {

    public InvalidVerificationTokenException(String message) {
        super(message);
    }
}
