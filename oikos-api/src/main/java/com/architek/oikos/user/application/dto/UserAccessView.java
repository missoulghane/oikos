package com.architek.oikos.user.application.dto;

import java.util.Set;
import java.util.stream.Collectors;

import com.architek.oikos.user.domain.model.PropertyRole;
import com.architek.oikos.user.domain.model.User;

/**
 * Internal, authorization-only projection of a User's access rights: the
 * caller (e.g. PropertyAccessEvaluator) never needs to know about
 * PropertyRoleGrant/PropertyRole internals, only the flattened sets it
 * exposes here (rule 6: cross-feature access only through a port-in view,
 * never the domain model or repository directly).
 */
public record UserAccessView(Set<String> globalRoles, Set<String> managedPropertyIds, Set<String> ownedPartyIds) {

    public static UserAccessView of(User user) {
        Set<String> globalRoles = user.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
        Set<String> managedPropertyIds = user.getPropertyRoleGrants().stream()
                .filter(grant -> grant.role() == PropertyRole.ROLE_PROPERTY_MANAGER)
                .map(grant -> grant.propertyId().toString())
                .collect(Collectors.toSet());
        Set<String> ownedPartyIds = user.getLinkedPartyIds().stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
        return new UserAccessView(globalRoles, managedPropertyIds, ownedPartyIds);
    }

    public boolean isAdmin() {
        return globalRoles.contains("ROLE_ADMIN") || globalRoles.contains("ROLE_MASTER");
    }

    public boolean managesProperty(String propertyId) {
        return managedPropertyIds.contains(propertyId);
    }
}
