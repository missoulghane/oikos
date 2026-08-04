package com.architek.oikos.installment.application.port.out;

import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to resolve every unit of a property together with its
 * currently configured price (per UnitTypePricing) or its shares
 * (tantiemes), so GenerateInstallmentCallUseCase can price a bulk
 * installment call - in either FLAT_RATE or SHARES dues calculation mode -
 * without knowing anything about property's persistence (rule 4).
 * Implemented in installment.infrastructure.adapter by delegating to
 * property's public port-in use cases (ListBuildingsByPropertyUseCase,
 * ListUnitsByBuildingUseCase, ListUnitTypePricesByPropertyUseCase).
 */
public interface PropertyUnitPricingPort {

    List<UnitPriceLine> listUnitPrices(EntityId propertyId);

    List<UnitShareLine> listUnitShares(EntityId propertyId);
}
