package com.architek.oikos.document.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.architek.oikos.document.domain.valueobject.DocumentId;
import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A file attached to a functional object (Property, Unit, ...) identified
 * generically by (ownerType, ownerId) rather than a dedicated FK per type.
 * storageKey is the server-generated key under which the binary content is
 * kept by whichever FileStoragePort adapter is active - never derived from
 * the user-supplied fileName, so it carries no path-traversal risk.
 */
public final class Document {

    private final DocumentId id;
    private final DocumentOwnerType ownerType;
    private final EntityId ownerId;
    private final String fileName;
    private final String contentType;
    private final long sizeBytes;
    private final String storageKey;
    private final String checksumSha256;
    private final EntityId uploadedBy;
    private final Instant uploadedAt;

    private Document(DocumentId id, DocumentOwnerType ownerType, EntityId ownerId, String fileName,
                      String contentType, long sizeBytes, String storageKey, String checksumSha256,
                      EntityId uploadedBy, Instant uploadedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.ownerType = Objects.requireNonNull(ownerType, "ownerType must not be null");
        this.ownerId = Objects.requireNonNull(ownerId, "ownerId must not be null");
        this.fileName = requireNonBlank(fileName, "fileName");
        this.contentType = requireNonBlank(contentType, "contentType");
        if (sizeBytes <= 0) {
            throw new IllegalArgumentException("sizeBytes must be > 0");
        }
        this.sizeBytes = sizeBytes;
        this.storageKey = requireNonBlank(storageKey, "storageKey");
        this.checksumSha256 = requireNonBlank(checksumSha256, "checksumSha256");
        this.uploadedBy = Objects.requireNonNull(uploadedBy, "uploadedBy must not be null");
        this.uploadedAt = Objects.requireNonNull(uploadedAt, "uploadedAt must not be null");
    }

    public static Document create(DocumentOwnerType ownerType, EntityId ownerId, String fileName, String contentType,
                                   long sizeBytes, String checksumSha256, EntityId uploadedBy) {
        DocumentId id = DocumentId.newId();
        return new Document(id, ownerType, ownerId, fileName, contentType, sizeBytes, id.toString(),
                checksumSha256, uploadedBy, Instant.now());
    }

    public static Document reconstruct(DocumentId id, DocumentOwnerType ownerType, EntityId ownerId, String fileName,
                                        String contentType, long sizeBytes, String storageKey, String checksumSha256,
                                        EntityId uploadedBy, Instant uploadedAt) {
        return new Document(id, ownerType, ownerId, fileName, contentType, sizeBytes, storageKey, checksumSha256,
                uploadedBy, uploadedAt);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public DocumentId getId() {
        return id;
    }

    public DocumentOwnerType getOwnerType() {
        return ownerType;
    }

    public EntityId getOwnerId() {
        return ownerId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getChecksumSha256() {
        return checksumSha256;
    }

    public EntityId getUploadedBy() {
        return uploadedBy;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Document other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
