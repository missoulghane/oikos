package com.architek.oikos.user.domain.model;

import java.util.Objects;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A single property-scoped role grant, held by an AppUser through one of its
 * linked Party rows. {@code partyId}/{@code propertyId} are the generic
 * {@link EntityId} (same decoupling rationale as {@code UnitOwnership.partyId}).
 * {@code propertyId} is denormalized from the granting party's own
 * property_id at write time (known to every caller that creates a grant),
 * so authorization checks can filter grants by property without reloading
 * each referenced Party.
 */
public record PropertyRoleGrant(EntityId partyId, EntityId propertyId, PropertyRole role) {

    public PropertyRoleGrant {
        Objects.requireNonNull(partyId, "partyId must not be null");
        Objects.requireNonNull(propertyId, "propertyId must not be null");
        Objects.requireNonNull(role, "role must not be null");
    }
}
