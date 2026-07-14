package com.architek.oikos.property.application.port.in;

import java.util.List;

import com.architek.oikos.property.application.dto.UnitOwnershipView;
import com.architek.oikos.property.application.query.ListUnitOwnershipsByUnitQuery;

public interface ListUnitOwnershipsByUnitUseCase {

    List<UnitOwnershipView> listUnitOwnerships(ListUnitOwnershipsByUnitQuery query);
}
