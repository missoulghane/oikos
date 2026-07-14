package com.architek.oikos.user.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

public class AccountAlreadyVerifiedException extends BusinessException {

    public AccountAlreadyVerifiedException() {
        super("Account is already activated");
    }
}
