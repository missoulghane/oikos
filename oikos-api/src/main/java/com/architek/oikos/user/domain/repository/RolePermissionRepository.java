package com.architek.oikos.user.domain.repository;

import java.util.Map;
import java.util.Set;

import com.architek.oikos.user.domain.model.Permission;

/**
 * Resolves the permission bundle behind a role name (spans both the
 * platform-wide {@code Role} enum, e.g. "ROLE_ADMIN", and the
 * property-scoped {@code PropertyRole} enum, e.g. "PROPERTY_BOARD_ADMIN") -
 * one lookup mechanism for both, since "which permissions does holding this
 * role name grant" is the same question for either. Data-driven (see the
 * role_permission table): editing a role's bundle never requires a code
 * change.
 */
public interface RolePermissionRepository {

    /** Roles with no configured bundle are simply absent from the result map (empty set of permissions). */
    Map<String, Set<Permission>> findPermissionsByRoleNames(Set<String> roleNames);
}
