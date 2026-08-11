package com.architek.oikos.accounting.web.response;

import java.math.BigDecimal;

import com.architek.oikos.accounting.application.dto.LedgerAccountView;
import com.architek.oikos.accounting.domain.valueobject.AccountNature;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;

public record LedgerAccountResponse(String id, String propertyId, String unitId, String accountNumber, String label,
                                     int accountClass, AccountNature nature, EntryDirection normalSide,
                                     boolean collective, AccountRole role, boolean active, BigDecimal balance,
                                     String bankAccountNumber) {

    public static LedgerAccountResponse from(LedgerAccountView view) {
        return new LedgerAccountResponse(view.id().toString(), view.propertyId() == null ? null : view.propertyId().toString(),
                view.unitId() == null ? null : view.unitId().toString(), view.accountNumber(), view.label(),
                view.accountClass(), view.nature(), view.normalSide(), view.collective(), view.role(), view.active(),
                view.balance(), view.bankAccountNumber());
    }
}
