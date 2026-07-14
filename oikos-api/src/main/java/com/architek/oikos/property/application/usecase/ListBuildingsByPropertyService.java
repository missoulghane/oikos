package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.dto.BuildingView;
import com.architek.oikos.property.application.port.in.ListBuildingsByPropertyUseCase;
import com.architek.oikos.property.application.query.ListBuildingsByPropertyQuery;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.shared.domain.pagination.Page;

@Component
public class ListBuildingsByPropertyService implements ListBuildingsByPropertyUseCase {

    private final BuildingRepository buildingRepository;

    public ListBuildingsByPropertyService(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BuildingView> listBuildings(ListBuildingsByPropertyQuery query) {
        return buildingRepository.findAllByPropertyId(query.propertyId(), query.pageRequest()).map(BuildingView::from);
    }
}
