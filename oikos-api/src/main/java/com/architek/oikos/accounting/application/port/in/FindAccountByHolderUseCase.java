package com.architek.oikos.accounting.application.port.in;

import java.util.Optional;

import com.architek.oikos.accounting.application.dto.AccountView;
import com.architek.oikos.accounting.application.query.GetAccountByHolderQuery;

/**
 * Non-throwing variant of GetAccountByHolderUseCase: returns empty rather
 * than throwing when no account exists. Exists specifically for callers that
 * treat "no account yet" as a normal, expected outcome (e.g. installment's
 * AccountLedgerPort skipping a unit without an account) - a RuntimeException
 * thrown out of another @Transactional service (GetAccountByHolderService)
 * marks the whole participating transaction rollback-only even if the caller
 * catches it, which breaks callers that need to keep committing other work
 * in the same transaction after a "not found" outcome.
 */
public interface FindAccountByHolderUseCase {

    Optional<AccountView> findAccount(GetAccountByHolderQuery query);
}
