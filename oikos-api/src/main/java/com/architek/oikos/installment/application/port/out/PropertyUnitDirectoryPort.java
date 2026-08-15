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

    default List<EntityId> listUnitIds(EntityId propertyId) {
        return listUnitIds(propertyId, null);
    }

    /**
     * search is handed to the property module untouched, so it matches exactly
     * what the lots list matches: unit number, owner name or phone. null lists
     * every unit of the property.
     */
    List<EntityId> listUnitIds(EntityId propertyId, String search);
}
