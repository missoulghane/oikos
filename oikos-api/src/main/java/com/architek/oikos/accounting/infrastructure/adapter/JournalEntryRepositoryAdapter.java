package com.architek.oikos.accounting.infrastructure.adapter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.domain.model.AuxiliaryUnitBalance;
import com.architek.oikos.accounting.domain.model.JournalEntry;
import com.architek.oikos.accounting.domain.repository.JournalEntryRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryFilter;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.accounting.infrastructure.mapper.JournalEntryPersistenceMapper;
import com.architek.oikos.accounting.infrastructure.persistence.JournalEntryEntity;
import com.architek.oikos.accounting.infrastructure.persistence.JournalEntryJpaRepository;
import com.architek.oikos.accounting.infrastructure.persistence.SequencePieceEntity;
import com.architek.oikos.accounting.infrastructure.persistence.SequencePieceJpaRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class JournalEntryRepositoryAdapter implements JournalEntryRepository {

    private final JournalEntryJpaRepository jpaRepository;
    private final SequencePieceJpaRepository sequencePieceJpaRepository;
    private final JournalEntryPersistenceMapper mapper;

    public JournalEntryRepositoryAdapter(JournalEntryJpaRepository jpaRepository,
                                          SequencePieceJpaRepository sequencePieceJpaRepository,
                                          JournalEntryPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.sequencePieceJpaRepository = sequencePieceJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public JournalEntry save(JournalEntry entry) {
        Optional<JournalEntryEntity> existing = jpaRepository.findById(entry.getId().asUuid());
        JournalEntryEntity saved;
        if (existing.isPresent()) {
            mapper.updateScalarFields(entry, existing.get());
            saved = jpaRepository.save(existing.get());
        } else {
            saved = jpaRepository.save(mapper.newEntity(entry));
        }
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<JournalEntry> findById(JournalEntryId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    /** I7: allocated under a row lock, portable across H2/Postgres via @Lock(PESSIMISTIC_WRITE). */
    @Override
    @Transactional
    public int nextPieceNumber(EntityId propertyId, AccountingExerciseId exerciseId, JournalCode journalCode) {
        Optional<SequencePieceEntity> existing = sequencePieceJpaRepository.findForUpdate(propertyId.value(),
                exerciseId.asUuid(), journalCode.name());
        if (existing.isPresent()) {
            SequencePieceEntity entity = existing.get();
            int allocated = entity.getNextNumber();
            entity.setNextNumber(allocated + 1);
            sequencePieceJpaRepository.save(entity);
            return allocated;
        }
        try {
            sequencePieceJpaRepository.saveAndFlush(
                    new SequencePieceEntity(propertyId.value(), exerciseId.asUuid(), journalCode.name(), 2));
            return 1;
        } catch (DataIntegrityViolationException raceLost) {
            return nextPieceNumber(propertyId, exerciseId, journalCode);
        }
    }

    @Override
    public List<JournalEntry> findAllByPeriodId(PeriodId periodId) {
        return jpaRepository.findAllByPeriodId(periodId.asUuid()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Page<JournalEntry> findPageByPropertyId(EntityId propertyId, PageRequest pageRequest) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest
                .of(pageRequest.pageNumber(), pageRequest.pageSize());
        org.springframework.data.domain.Page<JournalEntryEntity> page =
                jpaRepository.findAllByPropertyIdOrderByPieceDateDesc(propertyId.value(), pageable);
        return Page.of(page.getContent().stream().map(mapper::toDomain).toList(), page.getNumber(), page.getSize(),
                page.getTotalElements());
    }

    @Override
    public Page<JournalEntry> findPageByTreasuryAccount(EntityId propertyId, LedgerAccountId treasuryAccountId,
                                                          JournalEntryFilter filter, PageRequest pageRequest) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest
                .of(pageRequest.pageNumber(), pageRequest.pageSize());
        org.springframework.data.domain.Page<JournalEntryEntity> page = jpaRepository.searchByTreasuryAccount(
                propertyId.value(), treasuryAccountId.asUuid(), filter.pieceDateFrom(), filter.pieceDateTo(),
                filter.search(), filter.status() == null ? null : filter.status().name(), pageable);
        return Page.of(page.getContent().stream().map(mapper::toDomain).toList(), page.getNumber(), page.getSize(),
                page.getTotalElements());
    }

    @Override
    public BigDecimal sumNetAmountForAuxiliaryUnit(EntityId propertyId, LedgerAccountId accountId, EntityId unitId) {
        return jpaRepository.sumNetAmountForAuxiliaryUnit(propertyId.value(), accountId.asUuid(), unitId.value());
    }

    @Override
    public List<AuxiliaryUnitBalance> sumNetAmountGroupedByAuxiliaryUnit(EntityId propertyId, LedgerAccountId accountId) {
        return jpaRepository.sumNetAmountGroupedByAuxiliaryUnit(propertyId.value(), accountId.asUuid()).stream()
                .map(row -> new AuxiliaryUnitBalance(EntityId.of(row.getUnitId()), row.getAmount()))
                .toList();
    }
}
