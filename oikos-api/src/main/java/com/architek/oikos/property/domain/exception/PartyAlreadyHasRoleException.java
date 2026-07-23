package com.architek.oikos.property.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

public class ContactAlreadyHasRoleException extends BusinessException {

    public ContactAlreadyHasRoleException() {
        super("This contact already holds this management role on this property");
    }
}
