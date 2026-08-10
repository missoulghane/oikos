package com.architek.oikos.accounting.infrastructure.adapter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.infrastructure.mapper.LedgerAccountPersistenceMapper;
import com.architek.oikos.accounting.infrastructure.persistence.LedgerAccountEntity;
import com.architek.oikos.accounting.infrastructure.persistence.LedgerAccountJpaRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class LedgerAccountRepositoryAdapter implements LedgerAccountRepository {

    private final LedgerAccountJpaRepository jpaRepository;
    private final LedgerAccountPersistenceMapper mapper;

    public LedgerAccountRepositoryAdapter(LedgerAccountJpaRepository jpaRepository, LedgerAccountPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public LedgerAccount save(LedgerAccount account) {
        LedgerAccountEntity entity = jpaRepository.findById(account.getId().asUuid()).orElseGet(LedgerAccountEntity::new);
        mapper.toEntity(account, entity);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<LedgerAccount> findById(LedgerAccountId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public Optional<LedgerAccount> findGlobalByRole(AccountRole role) {
        return jpaRepository.findByPropertyIdIsNullAndRole(role.name()).map(mapper::toDomain);
    }

    @Override
    public Optional<LedgerAccount> findByPropertyIdAndRole(EntityId propertyId, AccountRole role) {
        return jpaRepository.findByPropertyIdAndUnitIdIsNullAndRole(propertyId.value(), role.name()).map(mapper::toDomain);
    }

    @Override
    public Optional<LedgerAccount> findByPropertyIdAndUnitIdAndRole(EntityId propertyId, EntityId unitId, AccountRole role) {
        return jpaRepository.findByPropertyIdAndUnitIdAndRole(propertyId.value(), unitId.value(), role.name())
                .map(mapper::toDomain);
    }

    @Override
    public List<LedgerAccount> findAllVisibleToProperty(EntityId propertyId) {
        return jpaRepository.findAllVisibleToProperty(propertyId.value()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void incrementBalance(LedgerAccountId id, BigDecimal signedDelta) {
        jpaRepository.incrementBalance(id.asUuid(), signedDelta);
    }
}
