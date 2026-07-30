package com.architek.oikos.accounting.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.port.in.FindUnitAccountByUnitUseCase;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class FindUnitAccountByUnitService implements FindUnitAccountByUnitUseCase {

    private final UnitAccountRepository unitAccountRepository;

    public FindUnitAccountByUnitService(UnitAccountRepository unitAccountRepository) {
        this.unitAccountRepository = unitAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EntityId> findByUnitId(EntityId unitId) {
        return unitAccountRepository.findByUnitId(unitId).map(account -> account.getId().value());
    }
}
