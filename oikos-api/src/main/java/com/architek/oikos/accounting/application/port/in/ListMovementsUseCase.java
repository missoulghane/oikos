package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.dto.MovementView;
import com.architek.oikos.accounting.application.query.ListMovementsQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListMovementsUseCase {

    Page<MovementView> listMovements(ListMovementsQuery query);
}
