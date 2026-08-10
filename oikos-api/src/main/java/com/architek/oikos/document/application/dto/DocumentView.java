package com.architek.oikos.document.application.dto;

import java.time.Instant;

import com.architek.oikos.document.domain.model.Document;
import com.architek.oikos.document.domain.valueobject.DocumentId;
import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record DocumentView(DocumentId id, DocumentOwnerType ownerType, EntityId ownerId, String fileName,
                            String contentType, long sizeBytes, EntityId uploadedBy, Instant uploadedAt) {

    public static DocumentView from(Document document) {
        return new DocumentView(document.getId(), document.getOwnerType(), document.getOwnerId(),
                document.getFileName(), document.getContentType(), document.getSizeBytes(),
                document.getUploadedBy(), document.getUploadedAt());
    }
}
