package com.architek.oikos.user.domain.exception;

import com.architek.oikos.shared.exception.ResourceNotFoundException;
import com.architek.oikos.user.domain.valueobject.UserId;

public class AvatarNotFoundException extends ResourceNotFoundException {

    public AvatarNotFoundException(UserId userId) {
        super("No avatar set for user: " + userId);
    }
}
