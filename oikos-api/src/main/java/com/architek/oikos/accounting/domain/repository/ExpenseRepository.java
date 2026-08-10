package com.architek.oikos.accounting.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.accounting.domain.model.Expense;
import com.architek.oikos.accounting.domain.valueobject.ExpenseId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface ExpenseRepository {

    Expense save(Expense expense);

    Optional<Expense> findById(ExpenseId id);

    List<Expense> findAllByPropertyId(EntityId propertyId);
}
