package com.architek.oikos.property.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

public class ContactAlreadyOwnsUnitException extends BusinessException {

    public ContactAlreadyOwnsUnitException() {
        super("This contact is already registered as an owner of this unit; remove the existing entry to change its share");
    }
}
