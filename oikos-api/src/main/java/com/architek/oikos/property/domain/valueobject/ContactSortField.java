package com.architek.oikos.property.domain.valueobject;

/**
 * Columns a contact listing can be ordered on. A closed set rather than a free
 * property name, for the same reason as UnitSortField.
 *
 * ACCOUNT_STATUS orders on whether the contact already has a user account -
 * ascending puts the ones still to invite first, which is the list a syndic
 * works through.
 */
public enum ContactSortField {
    FULL_NAME,
    ACCOUNT_STATUS
}
