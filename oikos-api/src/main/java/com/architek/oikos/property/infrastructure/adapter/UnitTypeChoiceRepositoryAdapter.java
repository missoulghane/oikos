package com.architek.oikos.property.infrastructure.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.architek.oikos.property.domain.model.UnitTypeChoice;
import com.architek.oikos.property.domain.repository.UnitTypeChoiceRepository;
import com.architek.oikos.property.infrastructure.persistence.UnitTypeChoiceEntity;
import com.architek.oikos.property.infrastructure.persistence.UnitTypeChoiceJpaRepository;

@Component
public class UnitTypeChoiceRepositoryAdapter implements UnitTypeChoiceRepository {

    private final UnitTypeChoiceJpaRepository jpaRepository;

    public UnitTypeChoiceRepositoryAdapter(UnitTypeChoiceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<UnitTypeChoice> findAllActive() {
        return jpaRepository.findByActiveTrueOrderByPositionAsc().stream().map(this::toDomain).toList();
    }

    private UnitTypeChoice toDomain(UnitTypeChoiceEntity entity) {
        return UnitTypeChoice.of(entity.getName(), entity.getPlural(), entity.getDescription(), entity.getPosition(),
                entity.isActive());
    }
}
