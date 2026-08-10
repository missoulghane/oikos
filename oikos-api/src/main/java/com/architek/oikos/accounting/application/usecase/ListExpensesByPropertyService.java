package com.architek.oikos.accounting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.application.dto.ExpenseView;
import com.architek.oikos.accounting.application.port.in.ListExpensesByPropertyUseCase;
import com.architek.oikos.accounting.application.query.ListExpensesByPropertyQuery;
import com.architek.oikos.accounting.domain.repository.ExpenseRepository;

@Component
public class ListExpensesByPropertyService implements ListExpensesByPropertyUseCase {

    private final ExpenseRepository expenseRepository;

    public ListExpensesByPropertyService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public List<ExpenseView> list(ListExpensesByPropertyQuery query) {
        return expenseRepository.findAllByPropertyId(query.propertyId()).stream().map(ExpenseView::from).toList();
    }
}
