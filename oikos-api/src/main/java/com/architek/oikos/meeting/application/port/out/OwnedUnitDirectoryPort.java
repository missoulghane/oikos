package com.architek.oikos.meeting.application.port.out;

import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The lots one account owns, for the owner's own "my meetings" list. Resolves
 * the account's parties and then their lots through user's and property's
 * port-in use cases; this module never learns what a UnitOwnership is.
 */
public interface OwnedUnitDirectoryPort {

    List<OwnedUnitInfo> listOwnedUnits(EntityId userId);
}
