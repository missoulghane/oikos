package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.exception.BusinessException;

/** A treasury-to-treasury transfer requires two distinct accounts. */
public class IdenticalTreasuryTransferAccountsException extends BusinessException {

    public IdenticalTreasuryTransferAccountsException(LedgerAccountId accountId) {
        super("Source and destination treasury accounts must differ, got " + accountId + " for both");
    }
}
