package com.architek.oikos.accounting.infrastructure.adapter;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.domain.repository.LedgerAccountNumberSequenceRepository;
import com.architek.oikos.accounting.infrastructure.persistence.LedgerAccountNumberSequenceEntity;
import com.architek.oikos.accounting.infrastructure.persistence.LedgerAccountNumberSequenceJpaRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Allocates under a row lock (SELECT ... FOR UPDATE via
 * @Lock(PESSIMISTIC_WRITE), portable across H2/Postgres through Hibernate).
 * The very first allocation for a given (property, prefix) has no row to
 * lock yet: it is inserted speculatively and, on the rare concurrent race
 * where two requests both try to create it, the loser's unique-constraint
 * violation is caught and retried - it will then find the row the winner
 * just created. Joins the caller's existing transaction (default
 * propagation) rather than committing independently, so a rolled-back
 * property/unit creation also rolls back the increment it allocated -
 * no permanent numbering gap from a failed request.
 */
@Component
public class LedgerAccountNumberSequenceRepositoryAdapter implements LedgerAccountNumberSequenceRepository {

    private final LedgerAccountNumberSequenceJpaRepository jpaRepository;

    public LedgerAccountNumberSequenceRepositoryAdapter(LedgerAccountNumberSequenceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public int allocateNextIncrement(EntityId propertyId, String numberPrefix) {
        Optional<LedgerAccountNumberSequenceEntity> existing =
                jpaRepository.findForUpdate(propertyId.value(), numberPrefix);
        if (existing.isPresent()) {
            LedgerAccountNumberSequenceEntity entity = existing.get();
            int allocated = entity.getNextIncrement();
            entity.setNextIncrement(allocated + 1);
            jpaRepository.save(entity);
            return allocated;
        }
        try {
            jpaRepository.saveAndFlush(new LedgerAccountNumberSequenceEntity(propertyId.value(), numberPrefix, 2));
            return 1;
        } catch (DataIntegrityViolationException raceLost) {
            return allocateNextIncrement(propertyId, numberPrefix);
        }
    }
}
