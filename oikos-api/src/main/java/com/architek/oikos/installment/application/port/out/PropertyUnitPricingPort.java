package com.architek.oikos.installment.application.port.out;

import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to resolve every unit of a property together with its
 * currently configured price (per UnitTypePricing), so
 * GenerateInstallmentCallUseCase can price a bulk installment call without
 * knowing anything about property's persistence (rule 4). Implemented in
 * accounting.infrastructure.adapter by delegating to property's public port-in
 * use cases (ListBuildingsByPropertyUseCase, ListUnitsByBuildingUseCase,
 * ListUnitTypePricesByPropertyUseCase).
 */
public interface PropertyUnitPricingPort {

    List<UnitPriceLine> listUnitPrices(EntityId propertyId);
}
