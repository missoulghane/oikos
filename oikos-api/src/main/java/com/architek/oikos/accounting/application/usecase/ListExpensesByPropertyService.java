package com.architek.oikos.accounting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.ExpenseView;
import com.architek.oikos.accounting.application.port.in.ListExpensesByPropertyUseCase;
import com.architek.oikos.accounting.application.query.ListExpensesByPropertyQuery;
import com.architek.oikos.accounting.domain.model.FinancialAccount;
import com.architek.oikos.accounting.domain.repository.ExpenseRepository;
import com.architek.oikos.accounting.domain.repository.FinancialAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.shared.domain.pagination.Page;

@Component
public class ListExpensesByPropertyService implements ListExpensesByPropertyUseCase {

    private final FinancialAccountRepository financialAccountRepository;
    private final ExpenseRepository expenseRepository;

    public ListExpensesByPropertyService(FinancialAccountRepository financialAccountRepository,
                                          ExpenseRepository expenseRepository) {
        this.financialAccountRepository = financialAccountRepository;
        this.expenseRepository = expenseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseView> list(ListExpensesByPropertyQuery query) {
        List<FinancialAccountId> accountIds = financialAccountRepository.findAllByPropertyId(query.propertyId())
                .stream().map(FinancialAccount::getId).toList();
        return expenseRepository.findPageByFinancialAccountIds(accountIds, query.pageRequest())
                .map(ExpenseView::from);
    }
}
