package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.exception.BusinessException;

/**
 * Moroccan cash-accounting rule enforced on owner payments: a CASH (caisse)
 * treasury account only ever receives cash ("especes") payments, and a BANK
 * account never receives one recorded as cash - the payment mode and the
 * targeted account's role must agree.
 */
public class TreasuryAccountPaymentModeMismatchException extends BusinessException {

    public TreasuryAccountPaymentModeMismatchException(LedgerAccountId treasuryAccountId, boolean isCashAccount) {
        super(isCashAccount
                ? "Treasury account " + treasuryAccountId + " is a cash account: payment mode must be CASH"
                : "Treasury account " + treasuryAccountId + " is a bank account: payment mode must not be CASH");
    }
}
