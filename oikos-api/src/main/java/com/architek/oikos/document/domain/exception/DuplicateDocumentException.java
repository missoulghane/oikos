package com.architek.oikos.document.domain.exception;

import com.architek.oikos.shared.exception.ConflictException;

/**
 * Thrown when the exact same content (same SHA-256 checksum) has already
 * been uploaded for the same owner - see UploadDocumentService.
 */
public class DuplicateDocumentException extends ConflictException {

    public DuplicateDocumentException(String fileName) {
        super("A document with the same content already exists for this entity: " + fileName);
    }
}
