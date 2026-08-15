package com.architek.oikos.installment.infrastructure.adapter;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import com.architek.oikos.installment.domain.repository.ReceiptNumberSequenceRepository;
import com.architek.oikos.installment.domain.valueobject.ReceiptNumber;
import com.architek.oikos.installment.infrastructure.persistence.SequenceReceiptEntity;
import com.architek.oikos.installment.infrastructure.persistence.SequenceReceiptJpaRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Mirrors JournalEntryRepositoryAdapter.nextPieceNumber, deliberately: allocated
 * under a pessimistic row lock so two payments recorded at the same instant can
 * never take the same number, portable across H2 and PostgreSQL.
 */
@Component
@RequiredArgsConstructor
public class ReceiptNumberSequenceAdapter implements ReceiptNumberSequenceRepository {

    private final SequenceReceiptJpaRepository jpaRepository;

    @Override
    @Transactional
    public ReceiptNumber allocate(EntityId propertyId, int year) {
        Optional<SequenceReceiptEntity> existing = jpaRepository.findForUpdate(propertyId.value(), year);
        if (existing.isPresent()) {
            SequenceReceiptEntity entity = existing.get();
            int allocated = entity.getNextNumber();
            entity.setNextNumber(allocated + 1);
            jpaRepository.save(entity);
            return new ReceiptNumber(year, allocated);
        }
        try {
            // First receipt of the series: seed it at 2, having just taken 1.
            jpaRepository.saveAndFlush(new SequenceReceiptEntity(propertyId.value(), year, 2));
            return new ReceiptNumber(year, 1);
        } catch (DataIntegrityViolationException raceLost) {
            // A concurrent transaction created the row first; retry, this time
            // taking the locked-read branch above.
            return allocate(propertyId, year);
        }
    }
}
