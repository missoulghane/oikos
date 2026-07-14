package com.architek.oikos.property.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.infrastructure.mapper.BuildingPersistenceMapper;
import com.architek.oikos.property.infrastructure.persistence.BuildingEntity;
import com.architek.oikos.property.infrastructure.persistence.BuildingJpaRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

@Component
public class BuildingRepositoryAdapter implements BuildingRepository {

    private final BuildingJpaRepository jpaRepository;
    private final BuildingPersistenceMapper mapper;

    public BuildingRepositoryAdapter(BuildingJpaRepository jpaRepository, BuildingPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Building save(Building building) {
        BuildingEntity entity = jpaRepository.findById(building.getId().asUuid()).orElseGet(BuildingEntity::new);
        BuildingEntity saved = jpaRepository.save(mapper.toEntity(building, entity));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Building> findById(BuildingId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public Page<Building> findAllByPropertyId(PropertyId propertyId, PageRequest pageRequest) {
        Pageable pageable = Pageable.ofSize(pageRequest.pageSize()).withPage(pageRequest.pageNumber());
        org.springframework.data.domain.Page<BuildingEntity> springPage =
                jpaRepository.findByPropertyId(propertyId.asUuid(), pageable);
        List<Building> content = springPage.getContent().stream().map(mapper::toDomain).toList();
        return Page.of(content, pageRequest.pageNumber(), pageRequest.pageSize(), springPage.getTotalElements());
    }
}
