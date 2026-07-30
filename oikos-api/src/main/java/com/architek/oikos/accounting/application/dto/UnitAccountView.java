package com.architek.oikos.accounting.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record UnitAccountView(UnitAccountId id, EntityId unitId, BigDecimal balance, Instant lastUpdatedDate) {

    public static UnitAccountView from(UnitAccount account) {
        return new UnitAccountView(account.getId(), account.getUnitId(), account.getBalance(),
                account.getLastUpdatedDate());
    }
}
