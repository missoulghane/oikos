package com.architek.oikos.user.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

public class LoginAlreadyUsedException extends BusinessException {

    public LoginAlreadyUsedException(String login) {
        super("A user account already exists with login: " + login);
    }
}
