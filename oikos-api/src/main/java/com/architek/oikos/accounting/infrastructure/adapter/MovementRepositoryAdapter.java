package com.architek.oikos.accounting.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.domain.model.Movement;
import com.architek.oikos.accounting.domain.repository.MovementRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.MovementId;
import com.architek.oikos.accounting.infrastructure.mapper.MovementPersistenceMapper;
import com.architek.oikos.accounting.infrastructure.persistence.MovementEntity;
import com.architek.oikos.accounting.infrastructure.persistence.MovementJpaRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

@Component
public class MovementRepositoryAdapter implements MovementRepository {

    private final MovementJpaRepository jpaRepository;
    private final MovementPersistenceMapper mapper;

    public MovementRepositoryAdapter(MovementJpaRepository jpaRepository, MovementPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Movement save(Movement movement) {
        MovementEntity entity = jpaRepository.findById(movement.getId().asUuid()).orElseGet(MovementEntity::new);
        mapper.toEntity(movement, entity);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Movement> findById(MovementId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public List<Movement> findAllByAccountId(AccountId accountId) {
        return jpaRepository.findAllByAccountId(accountId.asUuid()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Page<Movement> findPageByAccountId(AccountId accountId, PageRequest pageRequest) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest
                .of(pageRequest.pageNumber(), pageRequest.pageSize());
        org.springframework.data.domain.Page<MovementEntity> page =
                jpaRepository.findAllByAccountId(accountId.asUuid(), pageable);
        List<Movement> content = page.getContent().stream().map(mapper::toDomain).toList();
        return Page.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
