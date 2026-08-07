package com.architek.oikos.property.domain.valueobject;

/**
 * Lifecycle of a {@link com.architek.oikos.property.domain.model.BoardMember}.
 * A member added directly by an admin (AddBoardMemberService) starts and
 * stays ACTIVE. A member created from the invitation-acceptance flow
 * (CreatePendingBoardMemberService) starts PENDING_VALIDATION: the party
 * has accepted the invitation link, but no board-scoped role has been
 * granted yet - an admin must explicitly validate the seat
 * (ValidateBoardMemberService) before it becomes ACTIVE and the
 * PROPERTY_BOARD_MEMBER role is actually granted.
 */
public enum BoardMemberStatus {
    PENDING_VALIDATION,
    ACTIVE
}
