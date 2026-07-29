package com.architek.oikos.user.infrastructure.persistence;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Infrastructure-only JPA projection of a single app_user_party_role row
 * (party_id, property_id, role). Kept out of domain (see PropertyRoleGrant)
 * since @Embeddable is a JPA annotation, banned from domain by DependencyRulesArchTest.
 */
@Embeddable
public class PropertyRoleGrantEmbeddable {

    @Column(name = "party_id", nullable = false)
    private UUID partyId;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(name = "role", nullable = false)
    private String role;

    protected PropertyRoleGrantEmbeddable() {
    }

    public PropertyRoleGrantEmbeddable(UUID partyId, UUID propertyId, String role) {
        this.partyId = partyId;
        this.propertyId = propertyId;
        this.role = role;
    }

    public UUID getPartyId() {
        return partyId;
    }

    public UUID getPropertyId() {
        return propertyId;
    }

    public String getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof PropertyRoleGrantEmbeddable other
                && partyId.equals(other.partyId) && role.equals(other.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partyId, role);
    }
}
