package com.architek.oikos.property.application.usecase;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.dto.UnitTypePriceView;
import com.architek.oikos.property.application.port.in.ListUnitTypePricesByPropertyUseCase;
import com.architek.oikos.property.application.query.ListUnitTypePricesByPropertyQuery;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.repository.UnitTypePricingRepository;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

@Component
public class ListUnitTypePricesByPropertyService implements ListUnitTypePricesByPropertyUseCase {

    private final UnitTypePricingRepository unitTypePricingRepository;
    private final UnitTypeDefinitionRepository unitTypeDefinitionRepository;

    public ListUnitTypePricesByPropertyService(UnitTypePricingRepository unitTypePricingRepository,
                                                 UnitTypeDefinitionRepository unitTypeDefinitionRepository) {
        this.unitTypePricingRepository = unitTypePricingRepository;
        this.unitTypeDefinitionRepository = unitTypeDefinitionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitTypePriceView> listUnitTypePrices(ListUnitTypePricesByPropertyQuery query) {
        Map<UnitTypeDefinitionId, String> namesByUnitTypeId = unitTypeDefinitionRepository
                .findAllByPropertyId(query.propertyId()).stream()
                .collect(Collectors.toMap(UnitTypeDefinition::getId, UnitTypeDefinition::getName));

        return unitTypePricingRepository.findAllByPropertyId(query.propertyId()).stream()
                .map(pricing -> UnitTypePriceView.from(pricing, namesByUnitTypeId.get(pricing.getUnitTypeId())))
                .toList();
    }
}
