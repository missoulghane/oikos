package com.architek.oikos.document.domain.exception;

import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class DocumentNotFoundException extends ResourceNotFoundException {

    public DocumentNotFoundException(String documentId) {
        super("Document not found with id: " + documentId);
    }
}
