package com.architek.oikos.property.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

public class PartyAlreadyHasRoleException extends BusinessException {

    public PartyAlreadyHasRoleException() {
        super("This party already holds this management role on this property");
    }
}
