package com.architek.oikos.document.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

public class UnsupportedFileTypeException extends BusinessException {

    public UnsupportedFileTypeException(String contentType) {
        super("File type not allowed: " + contentType);
    }
}
