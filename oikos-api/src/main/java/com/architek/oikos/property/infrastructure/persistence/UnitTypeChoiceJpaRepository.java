package com.architek.oikos.property.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitTypeChoiceJpaRepository extends JpaRepository<UnitTypeChoiceEntity, String> {

    List<UnitTypeChoiceEntity> findByActiveTrueOrderByPositionAsc();
}
