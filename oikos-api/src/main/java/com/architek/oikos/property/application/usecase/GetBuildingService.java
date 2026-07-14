package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.dto.BuildingView;
import com.architek.oikos.property.application.port.in.GetBuildingUseCase;
import com.architek.oikos.property.application.query.GetBuildingQuery;
import com.architek.oikos.property.domain.exception.BuildingNotFoundException;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.repository.BuildingRepository;

@Component
public class GetBuildingService implements GetBuildingUseCase {

    private final BuildingRepository buildingRepository;

    public GetBuildingService(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public BuildingView getBuilding(GetBuildingQuery query) {
        Building building = buildingRepository.findById(query.id())
                .orElseThrow(() -> new BuildingNotFoundException(query.id()));
        return BuildingView.from(building);
    }
}
