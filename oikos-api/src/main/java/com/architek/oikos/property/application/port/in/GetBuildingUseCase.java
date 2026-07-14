package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.dto.BuildingView;
import com.architek.oikos.property.application.query.GetBuildingQuery;

public interface GetBuildingUseCase {

    BuildingView getBuilding(GetBuildingQuery query);
}
