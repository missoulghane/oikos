package com.architek.oikos.document.domain.repository;

import java.util.Optional;

import com.architek.oikos.document.domain.model.Document;
import com.architek.oikos.document.domain.valueobject.DocumentId;
import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface DocumentRepository {

    Document save(Document document);

    Optional<Document> findById(DocumentId id);

    /**
     * Most recent first. search, when not blank, keeps the documents whose
     * title or file name contains it, case-insensitively.
     */
    Page<Document> findAllByOwner(DocumentOwnerType ownerType, EntityId ownerId, String search,
                                  PageRequest pageRequest);

    void deleteById(DocumentId id);

    boolean existsByOwnerAndChecksum(DocumentOwnerType ownerType, EntityId ownerId, String checksumSha256);
}
