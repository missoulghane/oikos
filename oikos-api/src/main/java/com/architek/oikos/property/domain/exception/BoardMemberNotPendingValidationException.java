package com.architek.oikos.property.domain.exception;

import com.architek.oikos.shared.exception.ConflictException;

public class BoardMemberNotPendingValidationException extends ConflictException {

    public BoardMemberNotPendingValidationException() {
        super("This board member is not pending validation");
    }
}
