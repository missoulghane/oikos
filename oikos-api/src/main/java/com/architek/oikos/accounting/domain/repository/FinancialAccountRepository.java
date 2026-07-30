package com.architek.oikos.accounting.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.accounting.domain.model.FinancialAccount;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface FinancialAccountRepository {

    FinancialAccount save(FinancialAccount account);

    Optional<FinancialAccount> findById(FinancialAccountId id);

    List<FinancialAccount> findAllByPropertyId(EntityId propertyId);
}
