package com.architek.oikos.accounting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.UnitAccountView;
import com.architek.oikos.accounting.application.port.in.GetUnitAccountUseCase;
import com.architek.oikos.accounting.application.query.GetUnitAccountQuery;
import com.architek.oikos.accounting.domain.exception.UnitAccountNotFoundException;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;

@Component
public class GetUnitAccountService implements GetUnitAccountUseCase {

    private final UnitAccountRepository unitAccountRepository;

    public GetUnitAccountService(UnitAccountRepository unitAccountRepository) {
        this.unitAccountRepository = unitAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UnitAccountView get(GetUnitAccountQuery query) {
        return unitAccountRepository.findByUnitId(query.unitId())
                .map(UnitAccountView::from)
                .orElseThrow(() -> UnitAccountNotFoundException.forUnit(query.unitId()));
    }
}
