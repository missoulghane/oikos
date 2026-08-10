package com.architek.oikos.accounting.infrastructure.persistence;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PeriodJpaRepository extends JpaRepository<PeriodEntity, UUID> {

    Optional<PeriodEntity> findByExerciseIdAndYearMonth(UUID exerciseId, LocalDate yearMonth);
}
