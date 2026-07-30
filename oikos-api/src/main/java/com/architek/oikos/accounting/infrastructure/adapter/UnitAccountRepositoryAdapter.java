package com.architek.oikos.accounting.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.infrastructure.mapper.UnitAccountPersistenceMapper;
import com.architek.oikos.accounting.infrastructure.persistence.UnitAccountEntity;
import com.architek.oikos.accounting.infrastructure.persistence.UnitAccountJpaRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class UnitAccountRepositoryAdapter implements UnitAccountRepository {

    private final UnitAccountJpaRepository jpaRepository;
    private final UnitAccountPersistenceMapper mapper;

    public UnitAccountRepositoryAdapter(UnitAccountJpaRepository jpaRepository, UnitAccountPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public UnitAccount save(UnitAccount account) {
        UnitAccountEntity entity = jpaRepository.findById(account.getId().asUuid()).orElseGet(UnitAccountEntity::new);
        mapper.toEntity(account, entity);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<UnitAccount> findById(UnitAccountId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public Optional<UnitAccount> findByUnitId(EntityId unitId) {
        return jpaRepository.findByUnitId(unitId.value()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUnitId(EntityId unitId) {
        return jpaRepository.existsByUnitId(unitId.value());
    }

    @Override
    public List<UnitAccount> findAllByPropertyId(EntityId propertyId) {
        return jpaRepository.findAllByPropertyId(propertyId.value()).stream().map(mapper::toDomain).toList();
    }
}
