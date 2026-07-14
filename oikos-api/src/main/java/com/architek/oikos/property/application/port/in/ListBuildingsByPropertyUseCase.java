package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.dto.BuildingView;
import com.architek.oikos.property.application.query.ListBuildingsByPropertyQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListBuildingsByPropertyUseCase {

    Page<BuildingView> listBuildings(ListBuildingsByPropertyQuery query);
}
