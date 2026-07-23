package com.architek.oikos.property.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.property.domain.model.UnitTypePricing;
import com.architek.oikos.property.domain.repository.UnitTypePricingRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.infrastructure.mapper.UnitTypePricingPersistenceMapper;
import com.architek.oikos.property.infrastructure.persistence.UnitTypePricingEntity;
import com.architek.oikos.property.infrastructure.persistence.UnitTypePricingJpaRepository;

@Component
public class UnitTypePricingRepositoryAdapter implements UnitTypePricingRepository {

    private final UnitTypePricingJpaRepository jpaRepository;
    private final UnitTypePricingPersistenceMapper mapper;

    public UnitTypePricingRepositoryAdapter(UnitTypePricingJpaRepository jpaRepository,
                                              UnitTypePricingPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public UnitTypePricing save(UnitTypePricing unitTypePricing) {
        UnitTypePricingEntity entity = jpaRepository.findById(unitTypePricing.getId().asUuid())
                .orElseGet(UnitTypePricingEntity::new);
        UnitTypePricingEntity saved = jpaRepository.save(mapper.toEntity(unitTypePricing, entity));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<UnitTypePricing> findByUnitTypeId(UnitTypeDefinitionId unitTypeId) {
        return jpaRepository.findByUnitTypeId(unitTypeId.asUuid()).map(mapper::toDomain);
    }

    @Override
    public List<UnitTypePricing> findAllByPropertyId(PropertyId propertyId) {
        return jpaRepository.findByPropertyId(propertyId.asUuid()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteByUnitTypeId(UnitTypeDefinitionId unitTypeId) {
        jpaRepository.deleteByUnitTypeId(unitTypeId.asUuid());
    }
}
