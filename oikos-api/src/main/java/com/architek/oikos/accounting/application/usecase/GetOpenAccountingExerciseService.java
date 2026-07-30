package com.architek.oikos.accounting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.AccountingExerciseView;
import com.architek.oikos.accounting.application.port.in.GetOpenAccountingExerciseUseCase;
import com.architek.oikos.accounting.application.query.GetOpenAccountingExerciseQuery;
import com.architek.oikos.accounting.domain.exception.NoOpenExerciseException;
import com.architek.oikos.accounting.domain.repository.AccountingExerciseRepository;

@Component
public class GetOpenAccountingExerciseService implements GetOpenAccountingExerciseUseCase {

    private final AccountingExerciseRepository accountingExerciseRepository;

    public GetOpenAccountingExerciseService(AccountingExerciseRepository accountingExerciseRepository) {
        this.accountingExerciseRepository = accountingExerciseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountingExerciseView get(GetOpenAccountingExerciseQuery query) {
        return accountingExerciseRepository.findOpenByPropertyId(query.propertyId())
                .map(AccountingExerciseView::from)
                .orElseThrow(() -> new NoOpenExerciseException(query.propertyId()));
    }
}
