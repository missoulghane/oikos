package com.architek.oikos.contact.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

public class EmailAlreadyUsedException extends BusinessException {

    public EmailAlreadyUsedException(String email) {
        super("A contact already exists with email: " + email);
    }
}
