package com.architek.oikos.document.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

public class FileTooLargeException extends BusinessException {

    public FileTooLargeException(long sizeBytes, long maxSizeBytes) {
        super("File size " + sizeBytes + " bytes exceeds the maximum allowed size of " + maxSizeBytes + " bytes");
    }
}
