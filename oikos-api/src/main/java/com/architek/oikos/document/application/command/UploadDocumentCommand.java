package com.architek.oikos.document.application.command;

import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record UploadDocumentCommand(DocumentOwnerType ownerType, EntityId ownerId, String fileName,
                                     String contentType, byte[] content, EntityId uploadedBy) {
}
