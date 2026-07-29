package com.architek.oikos.user.domain.model;

/**
 * Groups the property-scoped roles by the business profile they belong to,
 * for rules that must branch on "which kind of profile is this" rather
 * than a specific permission (e.g. the property-creation cardinality rule -
 * see EnforcePropertyCreationLimitService). Not persisted directly; derived
 * from {@link PropertyRole#category()}.
 */
public enum RoleCategory {
    /** Volunteer syndic board (self-managed HOA) - capped in how many properties it may create. */
    BOARD,
    /** Professional property-management firm - uncapped. */
    MANAGER,
    /** Unit owner - never creates a property. */
    OWNER
}
