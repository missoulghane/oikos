package com.architek.oikos.accounting.infrastructure.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.domain.model.UnitAccountAllocation;
import com.architek.oikos.accounting.domain.repository.UnitAccountAllocationRepository;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.infrastructure.mapper.UnitAccountAllocationPersistenceMapper;
import com.architek.oikos.accounting.infrastructure.persistence.UnitAccountAllocationEntity;
import com.architek.oikos.accounting.infrastructure.persistence.UnitAccountAllocationJpaRepository;

@Component
public class UnitAccountAllocationRepositoryAdapter implements UnitAccountAllocationRepository {

    private final UnitAccountAllocationJpaRepository jpaRepository;
    private final UnitAccountAllocationPersistenceMapper mapper;

    public UnitAccountAllocationRepositoryAdapter(UnitAccountAllocationJpaRepository jpaRepository,
                                                   UnitAccountAllocationPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public UnitAccountAllocation save(UnitAccountAllocation allocation) {
        UnitAccountAllocationEntity entity = jpaRepository.findById(allocation.getId().asUuid())
                .orElseGet(UnitAccountAllocationEntity::new);
        mapper.toEntity(allocation, entity);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<UnitAccountAllocation> findAllByUnitAccountId(UnitAccountId unitAccountId) {
        return jpaRepository.findAllByUnitAccountId(unitAccountId.asUuid()).stream().map(mapper::toDomain).toList();
    }
}
