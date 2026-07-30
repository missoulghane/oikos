package com.architek.oikos.accounting.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseJpaRepository extends JpaRepository<ExpenseEntity, UUID> {

    Page<ExpenseEntity> findAllByFinancialAccountIdIn(List<UUID> financialAccountIds, Pageable pageable);
}
