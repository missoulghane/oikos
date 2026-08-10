package com.architek.oikos.document.domain.exception;

import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class DocumentOwnerNotFoundException extends ResourceNotFoundException {

    public DocumentOwnerNotFoundException(DocumentOwnerType ownerType, String ownerId) {
        super(ownerType + " not found with id: " + ownerId);
    }
}
