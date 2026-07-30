package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.dto.ExpenseView;
import com.architek.oikos.accounting.application.query.ListExpensesByPropertyQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListExpensesByPropertyUseCase {

    Page<ExpenseView> list(ListExpensesByPropertyQuery query);
}
