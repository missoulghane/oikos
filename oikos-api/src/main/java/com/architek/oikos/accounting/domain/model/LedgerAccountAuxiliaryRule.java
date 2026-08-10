package com.architek.oikos.accounting.domain.model;

import com.architek.oikos.accounting.domain.exception.CollectiveAccountRequiresAuxiliaryException;

/**
 * I6 (spec &sect;5): a collective account requires an auxiliary on every
 * line that moves it. Deliberately a pure function of already-resolved
 * domain objects (no repository access) so it can be unit-tested now and
 * reused as-is once a later phase's use cases resolve the LedgerAccount for
 * each line before building the JournalEntry.
 */
public final class LedgerAccountAuxiliaryRule {

    private LedgerAccountAuxiliaryRule() {
    }

    public static void validate(LedgerAccount account, JournalEntryLine line) {
        if (account.requiresAuxiliary() && !line.hasAuxiliary()) {
            throw new CollectiveAccountRequiresAuxiliaryException(account.getId());
        }
    }
}
