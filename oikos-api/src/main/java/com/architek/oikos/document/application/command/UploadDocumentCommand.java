package com.architek.oikos.document.application.command;

import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** title is optional - see Document.create. */
public record UploadDocumentCommand(DocumentOwnerType ownerType, EntityId ownerId, String fileName, String title,
                                     String contentType, byte[] content, EntityId uploadedBy) {

    /** Documents the application generates itself (receipts, convocations...) carry no title of their own. */
    public UploadDocumentCommand(DocumentOwnerType ownerType, EntityId ownerId, String fileName,
                                 String contentType, byte[] content, EntityId uploadedBy) {
        this(ownerType, ownerId, fileName, null, contentType, content, uploadedBy);
    }
}
