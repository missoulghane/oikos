package com.architek.oikos.accounting.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.port.in.CreateUnitAccountUseCase;
import com.architek.oikos.accounting.domain.exception.DuplicateUnitAccountException;
import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Public entry point for property's UnitAccountProvisioningPort: provisions a
 * unit's account as soon as the Unit itself is created, so a fund call never
 * has to auto-create it lazily (spec &sect;7 - one account per lot).
 */
@Component
public class CreateUnitAccountService implements CreateUnitAccountUseCase {

    private final UnitAccountRepository unitAccountRepository;
    private final Clock clock;

    public CreateUnitAccountService(UnitAccountRepository unitAccountRepository, Clock clock) {
        this.unitAccountRepository = unitAccountRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public EntityId create(EntityId unitId, EntityId propertyId) {
        if (unitAccountRepository.existsByUnitId(unitId)) {
            throw new DuplicateUnitAccountException(unitId);
        }
        UnitAccount account = UnitAccount.create(UnitAccountId.newId(), unitId, propertyId, clock.instant());
        return unitAccountRepository.save(account).getId().value();
    }
}
