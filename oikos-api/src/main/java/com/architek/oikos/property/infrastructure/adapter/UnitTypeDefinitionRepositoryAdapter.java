package com.architek.oikos.property.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.infrastructure.mapper.UnitTypeDefinitionPersistenceMapper;
import com.architek.oikos.property.infrastructure.persistence.UnitTypeDefinitionEntity;
import com.architek.oikos.property.infrastructure.persistence.UnitTypeDefinitionJpaRepository;

@Component
public class UnitTypeDefinitionRepositoryAdapter implements UnitTypeDefinitionRepository {

    private final UnitTypeDefinitionJpaRepository jpaRepository;
    private final UnitTypeDefinitionPersistenceMapper mapper;

    public UnitTypeDefinitionRepositoryAdapter(UnitTypeDefinitionJpaRepository jpaRepository,
                                                 UnitTypeDefinitionPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public UnitTypeDefinition save(UnitTypeDefinition unitTypeDefinition) {
        UnitTypeDefinitionEntity entity = jpaRepository.findById(unitTypeDefinition.getId().asUuid())
                .orElseGet(UnitTypeDefinitionEntity::new);
        UnitTypeDefinitionEntity saved = jpaRepository.save(mapper.toEntity(unitTypeDefinition, entity));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<UnitTypeDefinition> findById(UnitTypeDefinitionId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public Optional<UnitTypeDefinition> findByPropertyIdAndName(PropertyId propertyId, String name) {
        return jpaRepository.findByPropertyIdAndName(propertyId.asUuid(), name).map(mapper::toDomain);
    }

    @Override
    public List<UnitTypeDefinition> findAllByPropertyId(PropertyId propertyId) {
        return jpaRepository.findByPropertyId(propertyId.asUuid()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByPropertyIdAndName(PropertyId propertyId, String name) {
        return jpaRepository.existsByPropertyIdAndName(propertyId.asUuid(), name);
    }

    @Override
    public void deleteById(UnitTypeDefinitionId id) {
        jpaRepository.deleteById(id.asUuid());
    }
}
