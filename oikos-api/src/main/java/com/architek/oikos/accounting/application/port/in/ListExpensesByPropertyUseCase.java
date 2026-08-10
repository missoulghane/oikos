package com.architek.oikos.accounting.application.port.in;

import java.util.List;

import com.architek.oikos.accounting.application.dto.ExpenseView;
import com.architek.oikos.accounting.application.query.ListExpensesByPropertyQuery;

public interface ListExpensesByPropertyUseCase {

    List<ExpenseView> list(ListExpensesByPropertyQuery query);
}
