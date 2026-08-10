-- =========================================================================
-- V19: generic document management. A document is attached to a functional
-- object identified by (owner_type, owner_id) rather than a dedicated FK
-- per type (owner_type in ('PROPERTY', 'UNIT') today, see
-- document.domain.valueobject.DocumentOwnerType - adding a new attachable
-- type later is a code-only change, no schema migration needed). storage_key
-- is the server-generated key under which the binary content is kept by the
-- active FileStoragePort adapter (LocalDiskFileStorageAdapter today) - never
-- derived from file_name. checksum_sha256 + the unique constraint below are
-- the duplicate-detection guard (same content re-uploaded onto the same
-- owner is rejected, see UploadDocumentService).
-- =========================================================================

CREATE TABLE document (
    id                 UUID PRIMARY KEY,
    owner_type         VARCHAR(20) NOT NULL,
    owner_id           UUID NOT NULL,
    file_name          VARCHAR(255) NOT NULL,
    content_type       VARCHAR(100) NOT NULL,
    size_bytes         BIGINT NOT NULL,
    storage_key        VARCHAR(255) NOT NULL,
    checksum_sha256    VARCHAR(64) NOT NULL,
    uploaded_by        UUID NOT NULL REFERENCES app_user (id),
    created_date       TIMESTAMPTZ NOT NULL,
    last_modified_date TIMESTAMPTZ NOT NULL,
    version            BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_document_owner ON document (owner_type, owner_id);
CREATE UNIQUE INDEX uk_document_owner_checksum ON document (owner_type, owner_id, checksum_sha256);

INSERT INTO permission (key, description) VALUES
    ('document:read', 'Read/download documents attached to a property or unit'),
    ('document:write', 'Upload/delete documents attached to a property or unit');

INSERT INTO role_permission (role_name, permission_key) VALUES
    ('PROPERTY_BOARD_ADMIN', 'document:read'),
    ('PROPERTY_BOARD_ADMIN', 'document:write'),
    ('PROPERTY_BOARD_MEMBER', 'document:read'),
    ('PROPERTY_BOARD_MEMBER', 'document:write'),
    ('PROPERTY_MANAGER_ADMIN', 'document:read'),
    ('PROPERTY_MANAGER_ADMIN', 'document:write'),
    ('PROPERTY_MANAGER_MEMBER', 'document:read'),
    ('PROPERTY_MANAGER_MEMBER', 'document:write'),
    ('ROLE_ADMIN', 'document:read'),
    ('ROLE_ADMIN', 'document:write'),
    -- Read-only self-service, same shape as unit:read/installment:read - keeps
    -- the mechanism genuinely transverse even before an owner-facing UI exists.
    ('PROPERTY_OWNER', 'document:read');
