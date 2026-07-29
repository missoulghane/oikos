package com.architek.oikos.user.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

public class EmailAlreadyUsedException extends BusinessException {

    public EmailAlreadyUsedException(String email) {
        super("A user account already exists with email: " + email);
    }
}
