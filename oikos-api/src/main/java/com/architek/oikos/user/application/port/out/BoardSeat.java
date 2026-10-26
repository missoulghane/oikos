package com.architek.oikos.user.application.port.out;

/**
 * Seat a volunteer syndic chooses on their property's board when signing up
 * through the wizard - user's own view of property's BoardRole, decoupled from
 * it (rule 6: cross-feature access only through ports) and deliberately
 * narrower: PROPERTY_MANAGER belongs to a professional firm, which never goes
 * through this wizard. VOLUNTEER_MANAGER is "Syndic bénévole" (see
 * NOMENCLATURE.md), hence the wizard's default.
 */
public enum BoardSeat {
    VOLUNTEER_MANAGER,
    PRESIDENT,
    TREASURER,
    SECRETARY,
    MEMBER
}
