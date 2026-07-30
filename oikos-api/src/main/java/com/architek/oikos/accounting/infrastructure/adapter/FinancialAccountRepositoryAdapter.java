package com.architek.oikos.accounting.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.domain.model.FinancialAccount;
import com.architek.oikos.accounting.domain.repository.FinancialAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.infrastructure.mapper.FinancialAccountPersistenceMapper;
import com.architek.oikos.accounting.infrastructure.persistence.FinancialAccountEntity;
import com.architek.oikos.accounting.infrastructure.persistence.FinancialAccountJpaRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class FinancialAccountRepositoryAdapter implements FinancialAccountRepository {

    private final FinancialAccountJpaRepository jpaRepository;
    private final FinancialAccountPersistenceMapper mapper;

    public FinancialAccountRepositoryAdapter(FinancialAccountJpaRepository jpaRepository,
                                              FinancialAccountPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public FinancialAccount save(FinancialAccount account) {
        FinancialAccountEntity entity = jpaRepository.findById(account.getId().asUuid())
                .orElseGet(FinancialAccountEntity::new);
        mapper.toEntity(account, entity);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<FinancialAccount> findById(FinancialAccountId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public List<FinancialAccount> findAllByPropertyId(EntityId propertyId) {
        return jpaRepository.findAllByPropertyId(propertyId.value()).stream().map(mapper::toDomain).toList();
    }
}
