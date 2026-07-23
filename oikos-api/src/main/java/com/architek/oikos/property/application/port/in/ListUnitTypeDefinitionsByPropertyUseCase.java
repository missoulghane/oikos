package com.architek.oikos.property.application.port.in;

import java.util.List;

import com.architek.oikos.property.application.dto.UnitTypeDefinitionView;
import com.architek.oikos.property.application.query.ListUnitTypeDefinitionsByPropertyQuery;

public interface ListUnitTypeDefinitionsByPropertyUseCase {

    List<UnitTypeDefinitionView> listUnitTypeDefinitions(ListUnitTypeDefinitionsByPropertyQuery query);
}
