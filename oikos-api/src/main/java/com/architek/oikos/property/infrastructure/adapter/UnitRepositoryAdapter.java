package com.architek.oikos.property.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.infrastructure.mapper.UnitPersistenceMapper;
import com.architek.oikos.property.infrastructure.persistence.UnitEntity;
import com.architek.oikos.property.infrastructure.persistence.UnitJpaRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

@Component
public class UnitRepositoryAdapter implements UnitRepository {

    private final UnitJpaRepository jpaRepository;
    private final UnitPersistenceMapper mapper;

    public UnitRepositoryAdapter(UnitJpaRepository jpaRepository, UnitPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Unit save(Unit unit) {
        UnitEntity entity = jpaRepository.findById(unit.getId().asUuid()).orElseGet(UnitEntity::new);
        UnitEntity saved = jpaRepository.save(mapper.toEntity(unit, entity));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Unit> findById(UnitId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public Page<Unit> findAllByBuildingId(BuildingId buildingId, PageRequest pageRequest) {
        Pageable pageable = Pageable.ofSize(pageRequest.pageSize()).withPage(pageRequest.pageNumber());
        org.springframework.data.domain.Page<UnitEntity> springPage =
                jpaRepository.findByBuildingId(buildingId.asUuid(), pageable);
        List<Unit> content = springPage.getContent().stream().map(mapper::toDomain).toList();
        return Page.of(content, pageRequest.pageNumber(), pageRequest.pageSize(), springPage.getTotalElements());
    }

    @Override
    public boolean existsByUnitTypeId(UnitTypeDefinitionId unitTypeId) {
        return jpaRepository.existsByUnitTypeId(unitTypeId.asUuid());
    }
}
