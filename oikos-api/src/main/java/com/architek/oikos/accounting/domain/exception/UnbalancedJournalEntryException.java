package com.architek.oikos.accounting.domain.exception;

import java.math.BigDecimal;

import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.shared.exception.BusinessException;

/** I1: sum of debits must equal sum of credits at posting time (spec &sect;5/&sect;12: ECRITURE_DESEQUILIBREE). */
public class UnbalancedJournalEntryException extends BusinessException {

    private final BigDecimal difference;

    public UnbalancedJournalEntryException(JournalEntryId id, BigDecimal totalDebit, BigDecimal totalCredit) {
        super("Journal entry " + id + " is unbalanced: debit=" + totalDebit + ", credit=" + totalCredit);
        this.difference = totalDebit.subtract(totalCredit).abs();
    }

    /** The mismatch amount, to the cent (spec &sect;12: "avec l'ecart au centime"). */
    public BigDecimal getDifference() {
        return difference;
    }
}
