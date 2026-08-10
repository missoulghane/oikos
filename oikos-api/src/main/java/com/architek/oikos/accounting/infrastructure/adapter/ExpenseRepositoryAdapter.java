package com.architek.oikos.accounting.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.domain.model.Expense;
import com.architek.oikos.accounting.domain.repository.ExpenseRepository;
import com.architek.oikos.accounting.domain.valueobject.ExpenseId;
import com.architek.oikos.accounting.infrastructure.mapper.ExpensePersistenceMapper;
import com.architek.oikos.accounting.infrastructure.persistence.ExpenseJpaRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

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
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(expense)));
    }

    @Override
    public Optional<Expense> findById(ExpenseId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public List<Expense> findAllByPropertyId(EntityId propertyId) {
        return jpaRepository.findAllByPropertyId(propertyId.value()).stream().map(mapper::toDomain).toList();
    }
}
