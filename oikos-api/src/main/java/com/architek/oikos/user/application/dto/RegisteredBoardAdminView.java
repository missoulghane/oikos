package com.architek.oikos.user.application.dto;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * Outcome of a volunteer-syndic registration. Carries the property id because
 * the wizard's remaining steps configure that very property, and the caller
 * cannot look it up on its own: the account is not verified yet, so it cannot
 * log in and read its own grants.
 */
public record RegisteredBoardAdminView(UserId userId, EntityId propertyId) {
}
