package com.architek.oikos.accounting.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface UnitAccountRepository {

    UnitAccount save(UnitAccount account);

    Optional<UnitAccount> findById(UnitAccountId id);

    Optional<UnitAccount> findByUnitId(EntityId unitId);

    boolean existsByUnitId(EntityId unitId);

    List<UnitAccount> findAllByPropertyId(EntityId propertyId);
}
