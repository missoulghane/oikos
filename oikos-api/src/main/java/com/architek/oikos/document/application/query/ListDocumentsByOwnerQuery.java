package com.architek.oikos.document.application.query;

import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** search is optional: null or blank lists every document of the owner. */
public record ListDocumentsByOwnerQuery(DocumentOwnerType ownerType, EntityId ownerId, String search,
                                        PageRequest pageRequest) {

    public ListDocumentsByOwnerQuery(DocumentOwnerType ownerType, EntityId ownerId, PageRequest pageRequest) {
        this(ownerType, ownerId, null, pageRequest);
    }
}
