package com.architek.oikos.accounting.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.architek.oikos.accounting.domain.valueobject.ExerciseStatus;

public interface AccountingExerciseJpaRepository extends JpaRepository<AccountingExerciseEntity, UUID> {

    Optional<AccountingExerciseEntity> findByPropertyIdAndStatus(UUID propertyId, ExerciseStatus status);

    boolean existsByPropertyIdAndStatus(UUID propertyId, ExerciseStatus status);
}
