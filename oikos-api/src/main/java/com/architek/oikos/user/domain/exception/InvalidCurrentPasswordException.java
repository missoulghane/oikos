package com.architek.oikos.user.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

public class InvalidCurrentPasswordException extends BusinessException {

    public InvalidCurrentPasswordException() {
        super("The current password provided is incorrect");
    }
}
