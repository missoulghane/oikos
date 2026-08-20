package com.architek.oikos.installment.application.port.out;

import java.util.List;
import java.util.Map;

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

    /**
     * The same set as listUnitIds, keyed by unit id and carrying the lot number
     * as printed ("A12"). One walk of the property rather than one lookup per
     * row, which is what labelling a page of installments would otherwise cost.
     * Iteration order is the listing order, so callers can derive the unit ids
     * from the keys.
     */
    Map<EntityId, String> listUnitNumbersById(EntityId propertyId, String search);
}
