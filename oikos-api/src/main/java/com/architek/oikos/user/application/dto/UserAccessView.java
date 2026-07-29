package com.architek.oikos.user.application.dto;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.architek.oikos.user.domain.model.Permission;
import com.architek.oikos.user.domain.model.PropertyRole;

/**
 * Internal, authorization-only projection of a User's access rights: the
 * caller (e.g. PropertyAccessEvaluator) never needs to know about
 * PropertyRoleGrant internals, only the flattened views exposed here (rule
 * 6: cross-feature access only through a port-in view, never the domain
 * model or repository directly). Built by GetUserAccessService, which
 * resolves each held role's permission bundle through RolePermissionRepository.
 */
public record UserAccessView(Set<String> globalRoles,
                              Map<String, Set<PropertyRole>> rolesByProperty,
                              Map<String, Set<Permission>> permissionsByProperty,
                              Set<Permission> globalPermissions,
                              Set<String> ownedPartyIds) {

    public boolean isAdmin() {
        return globalRoles.contains("ROLE_ADMIN") || globalRoles.contains("ROLE_MASTER");
    }

    /** True for any property-scoped role at all (ADMIN or MEMBER tier, including OWNER). */
    public boolean managesProperty(String propertyId) {
        return isAdmin() || rolesByProperty.containsKey(propertyId);
    }

    public boolean hasPermission(String propertyId, Permission permission) {
        if (isAdmin() || globalPermissions.contains(permission)) {
            return true;
        }
        Set<Permission> permissions = permissionsByProperty.get(propertyId);
        return permissions != null && permissions.contains(permission);
    }

    /** Replaces isManagerOfAny: true if the caller holds property:create anywhere, or globally. */
    public boolean canCreateProperty() {
        if (isAdmin() || globalPermissions.contains(Permission.PROPERTY_CREATE)) {
            return true;
        }
        return permissionsByProperty.values().stream().anyMatch(permissions -> permissions.contains(Permission.PROPERTY_CREATE));
    }

    /**
     * The first ADMIN-tier role held across any property, used to infer which
     * role to grant when this caller creates another property (see
     * PropertyController.create). Assumes a caller holds at most one
     * ADMIN-tier role type across all their properties (never BOARD_ADMIN on
     * one and MANAGER_ADMIN on another).
     */
    public Optional<PropertyRole> dominantAdminTierRole() {
        return rolesByProperty.values().stream()
                .flatMap(Set::stream)
                .filter(PropertyRole::isAdminTier)
                .findFirst();
    }

    /** Number of properties this caller holds an ADMIN-tier role on - feeds the creation-cardinality rule. */
    public long countAdminTierProperties() {
        return rolesByProperty.values().stream()
                .filter(roles -> roles.stream().anyMatch(PropertyRole::isAdminTier))
                .count();
    }

    /** Properties this caller has any staff role on (ADMIN or MEMBER tier) - excludes plain OWNER-only entries. */
    public Set<String> managedPropertyIds() {
        return rolesByProperty.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(role -> role != PropertyRole.PROPERTY_OWNER))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
