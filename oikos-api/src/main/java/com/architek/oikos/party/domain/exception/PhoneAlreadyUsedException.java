package com.architek.oikos.party.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

public class PhoneAlreadyUsedException extends BusinessException {

    public PhoneAlreadyUsedException(String phone) {
        super("A party already exists with phone: " + phone);
    }
}
