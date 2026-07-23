package com.architek.oikos.property.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.dto.UnitTypeDefinitionView;
import com.architek.oikos.property.application.port.in.ListUnitTypeDefinitionsByPropertyUseCase;
import com.architek.oikos.property.application.query.ListUnitTypeDefinitionsByPropertyQuery;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;

@Component
public class ListUnitTypeDefinitionsByPropertyService implements ListUnitTypeDefinitionsByPropertyUseCase {

    private final UnitTypeDefinitionRepository unitTypeDefinitionRepository;

    public ListUnitTypeDefinitionsByPropertyService(UnitTypeDefinitionRepository unitTypeDefinitionRepository) {
        this.unitTypeDefinitionRepository = unitTypeDefinitionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitTypeDefinitionView> listUnitTypeDefinitions(ListUnitTypeDefinitionsByPropertyQuery query) {
        return unitTypeDefinitionRepository.findAllByPropertyId(query.propertyId()).stream()
                .map(UnitTypeDefinitionView::from).toList();
    }
}
