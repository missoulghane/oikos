package com.architek.oikos.user.domain.exception;

import com.architek.oikos.shared.exception.ResourceNotFoundException;
import com.architek.oikos.user.domain.valueobject.UserId;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(UserId id) {
        super("User not found with id: " + id);
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
