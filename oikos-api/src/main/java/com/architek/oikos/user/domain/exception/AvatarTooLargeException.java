package com.architek.oikos.user.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

public class AvatarTooLargeException extends BusinessException {

    public AvatarTooLargeException(long sizeBytes, long maxSizeBytes) {
        super("Avatar size " + sizeBytes + " bytes exceeds the maximum allowed size of " + maxSizeBytes + " bytes");
    }
}
