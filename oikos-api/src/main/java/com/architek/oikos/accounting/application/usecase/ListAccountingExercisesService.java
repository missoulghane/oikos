package com.architek.oikos.accounting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.AccountingExerciseView;
import com.architek.oikos.accounting.application.port.in.ListAccountingExercisesUseCase;
import com.architek.oikos.accounting.application.query.ListAccountingExercisesQuery;
import com.architek.oikos.accounting.domain.repository.AccountingExerciseRepository;

/** Every exercise of the property, oldest first - an empty list for a property that never opened one. */
@Component
public class ListAccountingExercisesService implements ListAccountingExercisesUseCase {

    private final AccountingExerciseRepository accountingExerciseRepository;

    public ListAccountingExercisesService(AccountingExerciseRepository accountingExerciseRepository) {
        this.accountingExerciseRepository = accountingExerciseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountingExerciseView> list(ListAccountingExercisesQuery query) {
        return accountingExerciseRepository.findAllByPropertyId(query.propertyId()).stream()
                .map(AccountingExerciseView::from)
                .toList();
    }
}
