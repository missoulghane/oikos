package com.architek.oikos.user.domain.model;

/**
 * Groups the property-scoped roles by the business profile they belong to,
 * for rules that must branch on "which kind of profile is this" rather
 * than a specific permission. Kept distinct from MANAGER even though the
 * permission bundle is currently identical (see V2__rbac_permissions.sql):
 * the distinction carries legal/regulatory weight (e.g. professional
 * syndic obligations) independent of any cardinality limit. Not persisted
 * directly; derived from {@link PropertyRole#category()}.
 */
public enum RoleCategory {
    /** Volunteer syndic board (self-managed HOA). */
    BOARD,
    /** Professional property-management firm. */
    MANAGER,
    /** Unit owner - never creates a property. */
    OWNER
}
