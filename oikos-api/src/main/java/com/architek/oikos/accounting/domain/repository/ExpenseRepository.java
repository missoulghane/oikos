package com.architek.oikos.accounting.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.accounting.domain.model.Expense;
import com.architek.oikos.accounting.domain.valueobject.ExpenseId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

public interface ExpenseRepository {

    Expense save(Expense expense);

    Optional<Expense> findById(ExpenseId id);

    /** financialAccountIds resolved by the caller from the property, same join-in-the-service pattern. */
    Page<Expense> findPageByFinancialAccountIds(List<FinancialAccountId> financialAccountIds, PageRequest pageRequest);
}
