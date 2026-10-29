package com.architek.oikos.installment.domain.valueobject;

import java.util.Objects;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/** A unit together with the property it belongs to - what a regularization needs to start. */
public record UnitOfProperty(EntityId propertyId, EntityId unitId) {

    public UnitOfProperty {
        Objects.requireNonNull(propertyId, "propertyId must not be null");
        Objects.requireNonNull(unitId, "unitId must not be null");
    }
}
