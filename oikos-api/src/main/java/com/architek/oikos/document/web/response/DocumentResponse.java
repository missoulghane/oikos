package com.architek.oikos.document.web.response;

import java.time.Instant;

import com.architek.oikos.document.application.dto.DocumentView;

public record DocumentResponse(String id, String ownerType, String ownerId, String fileName, String contentType,
                                long sizeBytes, String uploadedBy, Instant uploadedAt) {

    public static DocumentResponse from(DocumentView view) {
        return new DocumentResponse(view.id().toString(), view.ownerType().name(), view.ownerId().toString(),
                view.fileName(), view.contentType(), view.sizeBytes(), view.uploadedBy().toString(),
                view.uploadedAt());
    }
}
