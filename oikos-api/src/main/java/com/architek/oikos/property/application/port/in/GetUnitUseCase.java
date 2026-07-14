package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.query.GetUnitQuery;

public interface GetUnitUseCase {

    UnitView getUnit(GetUnitQuery query);
}
