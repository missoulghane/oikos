package com.architek.oikos.accounting.infrastructure.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.domain.model.FinancialJournalEntry;
import com.architek.oikos.accounting.domain.repository.FinancialJournalEntryRepository;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.FinancialJournalEntryFilter;
import com.architek.oikos.accounting.domain.valueobject.FinancialJournalEntryId;
import com.architek.oikos.accounting.infrastructure.mapper.FinancialJournalEntryPersistenceMapper;
import com.architek.oikos.accounting.infrastructure.persistence.FinancialJournalEntryEntity;
import com.architek.oikos.accounting.infrastructure.persistence.FinancialJournalEntryJpaRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

@Component
public class FinancialJournalEntryRepositoryAdapter implements FinancialJournalEntryRepository {

    private final FinancialJournalEntryJpaRepository jpaRepository;
    private final FinancialJournalEntryPersistenceMapper mapper;

    public FinancialJournalEntryRepositoryAdapter(FinancialJournalEntryJpaRepository jpaRepository,
                                                   FinancialJournalEntryPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public FinancialJournalEntry save(FinancialJournalEntry entry) {
        FinancialJournalEntryEntity entity = jpaRepository.findById(entry.getId().asUuid())
                .orElseGet(FinancialJournalEntryEntity::new);
        mapper.toEntity(entry, entity);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<FinancialJournalEntry> findById(FinancialJournalEntryId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public Page<FinancialJournalEntry> findPageByFinancialAccountIds(List<FinancialAccountId> financialAccountIds,
                                                                       FinancialJournalEntryFilter filter,
                                                                       PageRequest pageRequest) {
        List<UUID> accountIds = financialAccountIds.stream().map(FinancialAccountId::asUuid).toList();
        UUID exerciseId = filter.exerciseId() == null ? null : filter.exerciseId().asUuid();
        UUID filterAccountId = filter.financialAccountId() == null ? null : filter.financialAccountId().asUuid();
        List<UUID> effectiveAccountIds = filterAccountId == null ? accountIds : List.of(filterAccountId);

        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageRequest.pageNumber(),
                pageRequest.pageSize(), Sort.by(Sort.Direction.DESC, "date"));

        org.springframework.data.domain.Page<FinancialJournalEntryEntity> page = jpaRepository.search(
                effectiveAccountIds, exerciseId, filter.type(), filter.dateFrom(), filter.dateTo(), pageable);

        List<FinancialJournalEntry> content = page.getContent().stream().map(mapper::toDomain).toList();
        return Page.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
