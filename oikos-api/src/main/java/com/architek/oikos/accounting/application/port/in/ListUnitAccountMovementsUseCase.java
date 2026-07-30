package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.dto.UnitAccountMovementView;
import com.architek.oikos.accounting.application.query.ListUnitAccountMovementsQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListUnitAccountMovementsUseCase {

    Page<UnitAccountMovementView> list(ListUnitAccountMovementsQuery query);
}
