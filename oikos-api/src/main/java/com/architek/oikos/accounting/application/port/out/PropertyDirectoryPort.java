package com.architek.oikos.accounting.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to verify that a property referenced by an accounting
 * operation actually exists. Implemented in accounting.infrastructure.adapter
 * by delegating to property's public port-in (GetPropertyUseCase), never to
 * property's repository directly (rule 4/6).
 */
public interface PropertyDirectoryPort {

    boolean exists(EntityId propertyId);
}
