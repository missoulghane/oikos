package com.architek.oikos.meeting.application.port.out;

import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Every lot of a copropriété, with its tantièmes and its current owners.
 *
 * <p>"Every lot" is the point: a lot with no owner recorded is still a lot of
 * the copropriété, still convoked, and still counts in the total voting weight
 * that the quorum and an absolute majority are measured against (ADR 0002 §2).
 * Listing owners instead would silently drop it.
 *
 * <p>Implemented by MeetingPropertyUnitDirectoryAdapter through property's
 * public port-in use cases only (rule 4/6).
 */
public interface PropertyUnitDirectoryPort {

    List<UnitInfo> listUnits(EntityId propertyId);
}
