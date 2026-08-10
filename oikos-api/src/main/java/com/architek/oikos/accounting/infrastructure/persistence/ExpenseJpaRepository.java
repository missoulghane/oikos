package com.architek.oikos.accounting.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseJpaRepository extends JpaRepository<ExpenseEntity, UUID> {

    List<ExpenseEntity> findAllByPropertyId(UUID propertyId);
}
