package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.port.in.CountUnitsByPropertyUseCase;
import com.architek.oikos.property.application.query.CountUnitsByPropertyQuery;
import com.architek.oikos.property.domain.repository.UnitRepository;

@Component
public class CountUnitsByPropertyService implements CountUnitsByPropertyUseCase {

    private final UnitRepository unitRepository;

    public CountUnitsByPropertyService(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnits(CountUnitsByPropertyQuery query) {
        return unitRepository.countByPropertyId(query.propertyId());
    }
}
