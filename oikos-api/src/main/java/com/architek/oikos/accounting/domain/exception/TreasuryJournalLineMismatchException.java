package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.exception.BusinessException;

/**
 * Spec &sect;3.3: a treasury journal (BQ/CA) entry must have exactly one
 * line moving its attached treasury account.
 */
public class TreasuryJournalLineMismatchException extends BusinessException {

    public TreasuryJournalLineMismatchException(JournalCode journalCode, LedgerAccountId treasuryAccountId,
                                                 long matchingLineCount) {
        super("Journal " + journalCode + " entry must have exactly one line on treasury account "
                + treasuryAccountId + ", found: " + matchingLineCount);
    }
}
