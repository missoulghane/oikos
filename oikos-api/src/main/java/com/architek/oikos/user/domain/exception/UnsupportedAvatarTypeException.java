package com.architek.oikos.user.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

public class UnsupportedAvatarTypeException extends BusinessException {

    public UnsupportedAvatarTypeException(String contentType) {
        super("Avatar image type not allowed: " + contentType);
    }
}
