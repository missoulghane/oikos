package com.architek.oikos.accounting.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialAccountJpaRepository extends JpaRepository<FinancialAccountEntity, UUID> {

    List<FinancialAccountEntity> findAllByPropertyId(UUID propertyId);
}
