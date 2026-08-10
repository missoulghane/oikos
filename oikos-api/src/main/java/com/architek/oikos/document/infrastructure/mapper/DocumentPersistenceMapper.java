package com.architek.oikos.document.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.document.domain.model.Document;
import com.architek.oikos.document.domain.valueobject.DocumentId;
import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.document.infrastructure.persistence.DocumentEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface DocumentPersistenceMapper {

    default DocumentEntity toEntity(Document document) {
        return toEntity(document, new DocumentEntity());
    }

    default DocumentEntity toEntity(Document document, DocumentEntity entity) {
        entity.setId(document.getId().asUuid());
        entity.setOwnerType(document.getOwnerType().name());
        entity.setOwnerId(document.getOwnerId().value());
        entity.setFileName(document.getFileName());
        entity.setContentType(document.getContentType());
        entity.setSizeBytes(document.getSizeBytes());
        entity.setStorageKey(document.getStorageKey());
        entity.setChecksumSha256(document.getChecksumSha256());
        entity.setUploadedBy(document.getUploadedBy().value());
        return entity;
    }

    default Document toDomain(DocumentEntity entity) {
        return Document.reconstruct(DocumentId.of(entity.getId()), DocumentOwnerType.valueOf(entity.getOwnerType()),
                EntityId.of(entity.getOwnerId()), entity.getFileName(), entity.getContentType(),
                entity.getSizeBytes(), entity.getStorageKey(), entity.getChecksumSha256(),
                EntityId.of(entity.getUploadedBy()), entity.getCreatedDate());
    }
}
