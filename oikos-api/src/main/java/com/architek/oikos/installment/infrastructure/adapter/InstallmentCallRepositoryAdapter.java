package com.architek.oikos.installment.infrastructure.adapter;

import java.time.YearMonth;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.architek.oikos.installment.domain.model.InstallmentCall;
import com.architek.oikos.installment.domain.repository.InstallmentCallRepository;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.infrastructure.mapper.InstallmentCallPersistenceMapper;
import com.architek.oikos.installment.infrastructure.persistence.InstallmentCallEntity;
import com.architek.oikos.installment.infrastructure.persistence.InstallmentCallJpaRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class InstallmentCallRepositoryAdapter implements InstallmentCallRepository {

    private final InstallmentCallJpaRepository jpaRepository;
    private final InstallmentCallPersistenceMapper mapper;

    public InstallmentCallRepositoryAdapter(InstallmentCallJpaRepository jpaRepository, InstallmentCallPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public InstallmentCall save(InstallmentCall installmentCall) {
        InstallmentCallEntity entity = jpaRepository.findById(installmentCall.getId().asUuid())
                .orElseGet(InstallmentCallEntity::new);
        InstallmentCallEntity saved = jpaRepository.save(mapper.toEntity(installmentCall, entity));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<InstallmentCall> findById(InstallmentCallId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByPropertyIdAndPeriod(EntityId propertyId, YearMonth period) {
        return jpaRepository.existsByPropertyIdAndPeriod(propertyId.value(), period.atDay(1));
    }

    @Override
    public Page<InstallmentCall> findPageByPropertyId(EntityId propertyId, PageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageRequest.pageNumber(), pageRequest.pageSize(),
                Sort.by(Sort.Direction.DESC, "period"));
        org.springframework.data.domain.Page<InstallmentCallEntity> springPage = jpaRepository.findAllByPropertyId(
                propertyId.value(), pageable);
        return Page.of(springPage.getContent().stream().map(mapper::toDomain).toList(), springPage.getNumber(),
                springPage.getSize(), springPage.getTotalElements());
    }
}
