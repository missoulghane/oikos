package com.architek.oikos.document.application.port.out;

import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface DocumentOwnerExistencePort {

    boolean exists(DocumentOwnerType ownerType, EntityId ownerId);
}
