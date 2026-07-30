package com.architek.oikos.accounting.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.domain.model.UnitAccountMovement;
import com.architek.oikos.accounting.domain.repository.UnitAccountMovementRepository;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.accounting.infrastructure.mapper.UnitAccountMovementPersistenceMapper;
import com.architek.oikos.accounting.infrastructure.persistence.UnitAccountMovementEntity;
import com.architek.oikos.accounting.infrastructure.persistence.UnitAccountMovementJpaRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

@Component
public class UnitAccountMovementRepositoryAdapter implements UnitAccountMovementRepository {

    private final UnitAccountMovementJpaRepository jpaRepository;
    private final UnitAccountMovementPersistenceMapper mapper;

    public UnitAccountMovementRepositoryAdapter(UnitAccountMovementJpaRepository jpaRepository,
                                                 UnitAccountMovementPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public UnitAccountMovement save(UnitAccountMovement movement) {
        UnitAccountMovementEntity entity = jpaRepository.findById(movement.getId().asUuid())
                .orElseGet(UnitAccountMovementEntity::new);
        mapper.toEntity(movement, entity);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<UnitAccountMovement> findById(UnitAccountMovementId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public Page<UnitAccountMovement> findPageByUnitAccountId(UnitAccountId unitAccountId, PageRequest pageRequest) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest
                .of(pageRequest.pageNumber(), pageRequest.pageSize(),
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "date"));

        org.springframework.data.domain.Page<UnitAccountMovementEntity> page = jpaRepository
                .findAllByUnitAccountId(unitAccountId.asUuid(), pageable);

        List<UnitAccountMovement> content = page.getContent().stream().map(mapper::toDomain).toList();
        return Page.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    public List<UnitAccountMovement> findAllByUnitAccountId(UnitAccountId unitAccountId) {
        return jpaRepository.findAllByUnitAccountId(unitAccountId.asUuid()).stream().map(mapper::toDomain).toList();
    }
}
