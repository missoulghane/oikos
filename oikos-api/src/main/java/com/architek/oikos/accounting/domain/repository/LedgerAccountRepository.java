package com.architek.oikos.accounting.domain.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface LedgerAccountRepository {

    LedgerAccount save(LedgerAccount account);

    /** Atomic SQL-level increment (Partie: solde persiste) - never reads-modifies-writes the aggregate. */
    void incrementBalance(LedgerAccountId id, BigDecimal signedDelta);

    Optional<LedgerAccount> findById(LedgerAccountId id);

    /** Resolves a shared/global account (propertyId null) by its role - e.g. DUES_INCOME, SUPPLIER. */
    Optional<LedgerAccount> findGlobalByRole(AccountRole role);

    /** Resolves a property-scoped singleton account by role - e.g. a property's UNIT_ADVANCE account. */
    Optional<LedgerAccount> findByPropertyIdAndRole(EntityId propertyId, AccountRole role);

    /** Resolves a unit's own dedicated account for a given role (ADR 0001 decision 5) - e.g. its UNIT_RECEIVABLE account. */
    Optional<LedgerAccount> findByPropertyIdAndUnitIdAndRole(EntityId propertyId, EntityId unitId, AccountRole role);

    /** Every account usable by a property: the shared/global chart plus that property's own instances. */
    List<LedgerAccount> findAllVisibleToProperty(EntityId propertyId);
}
