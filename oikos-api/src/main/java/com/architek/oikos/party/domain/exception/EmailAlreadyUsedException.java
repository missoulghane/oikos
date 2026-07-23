package com.architek.oikos.party.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

public class EmailAlreadyUsedException extends BusinessException {

    public EmailAlreadyUsedException(String email) {
        super("A party already exists with email: " + email);
    }
}
