package com.architek.oikos.installment.application.port.out;

import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to verify that a property referenced by a cotisation
 * call actually exists, and to resolve its dues calculation configuration.
 * Implemented in installment.infrastructure.adapter by delegating to
 * property's public port-in (GetPropertyUseCase), never to property's
 * repository directly (rule 4).
 */
public interface PropertyDirectoryPort {

    boolean exists(EntityId propertyId);

    PropertyDuesConfigurationView getDuesConfiguration(EntityId propertyId);

    /** Display name of the copropriété, for documents addressed to an owner (payment receipts). */
    String getName(EntityId propertyId);

    /**
     * Every copropriété the product knows of. The one read here that is not
     * scoped to a property, because its caller cannot be: the imputation sweep
     * (RegularizeDueInstallmentsService) has to visit them all to find the
     * echeances that fell due today.
     */
    List<EntityId> listAllIds();
}
