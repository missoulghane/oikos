package com.architek.oikos.property.domain.exception;

import com.architek.oikos.property.domain.valueobject.BoardMemberId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class BoardMemberNotFoundException extends ResourceNotFoundException {

    public BoardMemberNotFoundException(BoardMemberId id) {
        super("BoardMember not found with id: " + id);
    }
}
