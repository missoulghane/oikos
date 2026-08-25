package com.architek.oikos.messaging.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.messaging.domain.model.RecipientGroup;
import com.architek.oikos.messaging.domain.valueobject.RecipientGroupId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface RecipientGroupRepository {

    RecipientGroup save(RecipientGroup group);

    Optional<RecipientGroup> findById(RecipientGroupId id);

    /** Les groupes d'une copropriété, par nom. */
    List<RecipientGroup> findAllByPropertyId(EntityId propertyId);

    void deleteById(RecipientGroupId id);
}
