package com.architek.oikos.user.application.port.out;

import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to resolve the units owned by a given Party, on behalf
 * of the "my units" self-service view (a USER's read-only access to their
 * own lots). Implemented in user.infrastructure.adapter by delegating to
 * property's public port-in use cases - never to property's repositories
 * directly (rule 6).
 */
public interface UnitDirectoryPort {

    List<OwnedUnitView> listUnitsOwnedByParty(EntityId partyId);
}
