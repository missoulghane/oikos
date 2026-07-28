package com.architek.oikos.installment.application.port.out;

import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to resolve every unit belonging to a property (a
 * "résidence"), so installment can list installments across a whole property.
 * Implemented in installment.infrastructure.adapter by delegating to property's
 * public port-in use cases (ListBuildingsByPropertyUseCase,
 * ListUnitsByBuildingUseCase), never to property's repositories directly
 * (rule 4).
 */
public interface PropertyUnitDirectoryPort {

    List<EntityId> listUnitIds(EntityId propertyId);
}
