package com.architek.oikos.auth.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

public class InvalidPasswordResetTokenException extends BusinessException {

    public InvalidPasswordResetTokenException(String message) {
        super(message);
    }
}
