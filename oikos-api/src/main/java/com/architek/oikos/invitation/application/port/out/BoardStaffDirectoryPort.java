package com.architek.oikos.invitation.application.port.out;

import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Resolves which platform accounts should be notified of board/manager-facing
 * events for a property (e.g. a new membership request landing on their
 * desk) - active board seats only. Mirrors messaging's own
 * PropertyMemberDirectoryPort staff-filtering rule (ACTIVE BoardMember rows),
 * never property's repositories directly (rule 6).
 */
public interface BoardStaffDirectoryPort {

    /** Only staff accounts with an actual linked platform account appear - a board seat with no linked account is silently skipped. */
    List<EntityId> listStaffUserIds(EntityId propertyId);
}
