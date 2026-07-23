package com.architek.oikos.installment.infrastructure.persistence;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstallmentCallJpaRepository extends JpaRepository<InstallmentCallEntity, UUID> {

    boolean existsByPropertyIdAndPeriod(UUID propertyId, LocalDate period);

    Page<InstallmentCallEntity> findAllByPropertyId(UUID propertyId, Pageable pageable);
}
