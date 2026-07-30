package com.architek.oikos.accounting.infrastructure.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.domain.model.Expense;
import com.architek.oikos.accounting.domain.repository.ExpenseRepository;
import com.architek.oikos.accounting.domain.valueobject.ExpenseId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.infrastructure.mapper.ExpensePersistenceMapper;
import com.architek.oikos.accounting.infrastructure.persistence.ExpenseEntity;
import com.architek.oikos.accounting.infrastructure.persistence.ExpenseJpaRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

@Component
public class ExpenseRepositoryAdapter implements ExpenseRepository {

    private final ExpenseJpaRepository jpaRepository;
    private final ExpensePersistenceMapper mapper;

    public ExpenseRepositoryAdapter(ExpenseJpaRepository jpaRepository, ExpensePersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Expense save(Expense expense) {
        ExpenseEntity entity = jpaRepository.findById(expense.getId().asUuid()).orElseGet(ExpenseEntity::new);
        mapper.toEntity(expense, entity);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Expense> findById(ExpenseId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public Page<Expense> findPageByFinancialAccountIds(List<FinancialAccountId> financialAccountIds,
                                                        PageRequest pageRequest) {
        List<UUID> accountIds = financialAccountIds.stream().map(FinancialAccountId::asUuid).toList();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest
                .of(pageRequest.pageNumber(), pageRequest.pageSize());

        org.springframework.data.domain.Page<ExpenseEntity> page = jpaRepository.findAllByFinancialAccountIdIn(
                accountIds, pageable);

        List<Expense> content = page.getContent().stream().map(mapper::toDomain).toList();
        return Page.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
