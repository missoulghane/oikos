package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.query.ListAvailableUnitsByPropertyQuery;
import com.architek.oikos.shared.domain.pagination.Page;

/**
 * Lists units with no owner yet (OwnershipStatus.NOT_AFFECTED), across every
 * building of a property - unlike ListUnitsByBuildingUseCase, which is
 * scoped to a single building. Used by the invitation feature's unit-picker
 * (PRIVATE_WITHOUT_UNIT / PUBLIC types).
 */
public interface ListAvailableUnitsByPropertyUseCase {

    Page<UnitView> listAvailableUnits(ListAvailableUnitsByPropertyQuery query);
}
