package com.architek.oikos.property.infrastructure.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.architek.oikos.property.domain.model.BoardRoleOption;
import com.architek.oikos.property.domain.repository.BoardRoleOptionRepository;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.property.infrastructure.persistence.BoardRoleOptionEntity;
import com.architek.oikos.property.infrastructure.persistence.BoardRoleOptionJpaRepository;

@Component
public class BoardRoleOptionRepositoryAdapter implements BoardRoleOptionRepository {

    private final BoardRoleOptionJpaRepository jpaRepository;

    public BoardRoleOptionRepositoryAdapter(BoardRoleOptionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<BoardRoleOption> findAllActive() {
        return jpaRepository.findByActiveTrueOrderByPositionAsc().stream().map(this::toDomain).toList();
    }

    private BoardRoleOption toDomain(BoardRoleOptionEntity entity) {
        return BoardRoleOption.of(BoardRole.valueOf(entity.getCode()), entity.getLabel(), entity.getPosition(),
                entity.isAvailableAtSignup(), entity.isActive());
    }
}
