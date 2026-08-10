package com.architek.oikos.accounting.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.valueobject.AccountNature;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record LedgerAccountView(LedgerAccountId id, EntityId propertyId, EntityId unitId, String accountNumber,
                                 String label, int accountClass, AccountNature nature, EntryDirection normalSide,
                                 boolean collective, AccountRole role, boolean active, BigDecimal balance) {

    public static LedgerAccountView from(LedgerAccount account) {
        return new LedgerAccountView(account.getId(), account.getPropertyId().orElse(null),
                account.getUnitId().orElse(null), account.getAccountNumber().value(), account.getLabel(),
                account.getAccountClass(), account.getNature(), account.getNormalSide(), account.isCollective(),
                account.getRole().orElse(null), account.isActive(), account.getBalance());
    }
}
