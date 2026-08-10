package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.exception.BusinessException;

/** I6: a collective account always needs an auxiliary on the line that moves it (spec &sect;12: AUXILIAIRE_REQUIS). */
public class CollectiveAccountRequiresAuxiliaryException extends BusinessException {

    public CollectiveAccountRequiresAuxiliaryException(LedgerAccountId accountId) {
        super("Ledger account " + accountId + " is collective and requires an auxiliary on every line that moves it");
    }
}
