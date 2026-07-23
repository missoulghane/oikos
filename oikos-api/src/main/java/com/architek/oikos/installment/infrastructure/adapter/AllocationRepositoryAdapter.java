package com.architek.oikos.installment.infrastructure.adapter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.installment.domain.model.Allocation;
import com.architek.oikos.installment.domain.repository.AllocationRepository;
import com.architek.oikos.installment.domain.valueobject.AllocationId;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.installment.infrastructure.mapper.AllocationPersistenceMapper;
import com.architek.oikos.installment.infrastructure.persistence.AllocationEntity;
import com.architek.oikos.installment.infrastructure.persistence.AllocationJpaRepository;

@Component
public class AllocationRepositoryAdapter implements AllocationRepository {

    private final AllocationJpaRepository jpaRepository;
    private final AllocationPersistenceMapper mapper;

    public AllocationRepositoryAdapter(AllocationJpaRepository jpaRepository, AllocationPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Allocation save(Allocation allocation) {
        AllocationEntity entity = jpaRepository.findById(allocation.getId().asUuid()).orElseGet(AllocationEntity::new);
        mapper.toEntity(allocation, entity);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Allocation> findById(AllocationId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public void deleteById(AllocationId id) {
        jpaRepository.deleteById(id.asUuid());
    }

    @Override
    public List<Allocation> findAllByInstallmentId(InstallmentId installmentId) {
        return jpaRepository.findAllByInstallmentId(installmentId.asUuid()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Allocation> findAllByMovementId(EntityId movementId) {
        return jpaRepository.findAllByMovementId(movementId.value()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public BigDecimal sumAllocatedByMovementId(EntityId movementId) {
        return jpaRepository.sumAllocatedAmountByMovementId(movementId.value());
    }

    @Override
    public BigDecimal sumAllocatedByInstallmentId(InstallmentId installmentId) {
        return jpaRepository.sumAllocatedAmountByInstallmentId(installmentId.asUuid());
    }
}
