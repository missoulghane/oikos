package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.query.ListUnitsByBuildingQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListUnitsByBuildingUseCase {

    Page<UnitView> listUnits(ListUnitsByBuildingQuery query);
}
