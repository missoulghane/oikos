package com.architek.oikos.property.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.property.infrastructure.mapper.UnitOwnershipPersistenceMapper;
import com.architek.oikos.property.infrastructure.persistence.UnitOwnershipEntity;
import com.architek.oikos.property.infrastructure.persistence.UnitOwnershipJpaRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class UnitOwnershipRepositoryAdapter implements UnitOwnershipRepository {

    private final UnitOwnershipJpaRepository jpaRepository;
    private final UnitOwnershipPersistenceMapper mapper;

    public UnitOwnershipRepositoryAdapter(UnitOwnershipJpaRepository jpaRepository, UnitOwnershipPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public UnitOwnership save(UnitOwnership unitOwnership) {
        UnitOwnershipEntity entity = jpaRepository.findById(unitOwnership.getId().asUuid()).orElseGet(UnitOwnershipEntity::new);
        UnitOwnershipEntity saved = jpaRepository.save(mapper.toEntity(unitOwnership, entity));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<UnitOwnership> findById(UnitOwnershipId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public List<UnitOwnership> findAllByUnitId(UnitId unitId) {
        return jpaRepository.findByUnitId(unitId.asUuid()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByUnitIdAndContactId(UnitId unitId, EntityId contactId) {
        return jpaRepository.existsByUnitIdAndContactId(unitId.asUuid(), contactId.value());
    }

    @Override
    public void deleteById(UnitOwnershipId id) {
        jpaRepository.deleteById(id.asUuid());
    }
}
