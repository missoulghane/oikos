package com.architek.oikos.accounting.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountingExerciseJpaRepository extends JpaRepository<AccountingExerciseEntity, UUID> {

    List<AccountingExerciseEntity> findAllByPropertyIdOrderByStartDateAsc(UUID propertyId);

    @Query("""
            select e from AccountingExerciseEntity e
            where e.propertyId = :propertyId and e.startDate <= :date and e.endDate >= :date
            """)
    Optional<AccountingExerciseEntity> findCoveringDate(@Param("propertyId") UUID propertyId,
                                                         @Param("date") LocalDate date);
}
