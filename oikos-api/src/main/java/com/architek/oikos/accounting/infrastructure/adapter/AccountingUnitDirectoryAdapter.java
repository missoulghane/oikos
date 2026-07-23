package com.architek.oikos.accounting.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.application.port.out.UnitDirectoryPort;
import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.port.in.GetBuildingUseCase;
import com.architek.oikos.property.application.port.in.GetUnitUseCase;
import com.architek.oikos.property.application.query.GetBuildingQuery;
import com.architek.oikos.property.application.query.GetUnitQuery;
import com.architek.oikos.property.domain.exception.UnitNotFoundException;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: delegates to property's public port-in
 * (GetUnitUseCase, GetBuildingUseCase), never to property's repository
 * directly (rule 4). Named distinctly from property's own
 * PropertyPartyDirectoryAdapter and user's PartyDirectoryAdapter to avoid a
 * Spring bean name collision between same-named classes in different
 * packages.
 */
@Component
public class AccountingUnitDirectoryAdapter implements UnitDirectoryPort {

    private final GetUnitUseCase getUnitUseCase;
    private final GetBuildingUseCase getBuildingUseCase;

    public AccountingUnitDirectoryAdapter(GetUnitUseCase getUnitUseCase, GetBuildingUseCase getBuildingUseCase) {
        this.getUnitUseCase = getUnitUseCase;
        this.getBuildingUseCase = getBuildingUseCase;
    }

    @Override
    public boolean exists(EntityId unitId) {
        try {
            getUnitUseCase.getUnit(new GetUnitQuery(UnitId.of(unitId.value())));
            return true;
        } catch (UnitNotFoundException e) {
            return false;
        }
    }

    @Override
    public EntityId resolvePropertyId(EntityId unitId) {
        UnitView unit = getUnitUseCase.getUnit(new GetUnitQuery(UnitId.of(unitId.value())));
        return getBuildingUseCase.getBuilding(new GetBuildingQuery(unit.buildingId())).propertyId().value();
    }
}
