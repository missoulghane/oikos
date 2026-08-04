package com.architek.oikos.invitation.application.port.out;

import java.util.Optional;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to read a property's identity for the public invitation
 * landing page. Implemented in invitation.infrastructure.adapter by
 * delegating to property's GetPropertyUseCase - never to property's
 * repository directly (rule 6).
 */
public interface PropertyDirectoryPort {

    Optional<PropertyBasicInfo> findBasicInfo(EntityId propertyId);
}
