package com.architek.oikos.document.application.query;

import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ListDocumentsByOwnerQuery(DocumentOwnerType ownerType, EntityId ownerId, PageRequest pageRequest) {
}
