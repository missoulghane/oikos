package com.architek.oikos.accounting.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.architek.oikos.accounting.domain.valueobject.AccountType;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {

    Optional<AccountEntity> findByHolderIdAndAccountType(UUID holderId, AccountType accountType);

    boolean existsByHolderIdAndAccountType(UUID holderId, AccountType accountType);
}
