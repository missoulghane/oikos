package com.architek.oikos.meeting.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port resolving the handful of things this module needs to know
 * about a copropriété: that it exists, what it is called, and how its voices
 * are weighted. Implemented by MeetingPropertyDirectoryAdapter, which
 * delegates to property's public port-in use cases only - meeting never
 * touches property's domain model, repositories or infrastructure (rule 4/6,
 * enforced by DependencyRulesArchTest).
 */
public interface PropertyDirectoryPort {

    /** Throws property's own not-found exception (mapped to 404) if no such property exists. */
    PropertyInfo getProperty(EntityId propertyId);
}
