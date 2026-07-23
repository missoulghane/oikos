package com.architek.oikos.installment.infrastructure.adapter;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.application.command.RecordDebitCommand;
import com.architek.oikos.accounting.application.port.in.FindAccountByHolderUseCase;
import com.architek.oikos.accounting.application.port.in.RecordDebitUseCase;
import com.architek.oikos.accounting.application.query.GetAccountByHolderQuery;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.installment.application.port.out.AccountLedgerPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: reaches accounting's ledger by delegating to its public
 * port-in (FindAccountByHolderUseCase, RecordDebitUseCase), never to accounting's
 * repositories or domain model directly (rule 4). Uses the non-throwing
 * FindAccountByHolderUseCase rather than GetAccountByHolderUseCase
 * specifically because "no account yet" is an expected outcome here (a unit
 * to skip, not an error) - a RuntimeException thrown out of another
 * @Transactional service marks the whole participating transaction
 * rollback-only even once caught here, which would silently abort
 * GenerateInstallmentCallService's batch after the first skipped unit.
 */
@Component
public class InstallmentAccountLedgerAdapter implements AccountLedgerPort {

    private final FindAccountByHolderUseCase findAccountByHolderUseCase;
    private final RecordDebitUseCase recordDebitUseCase;

    public InstallmentAccountLedgerAdapter(FindAccountByHolderUseCase findAccountByHolderUseCase,
                                            RecordDebitUseCase recordDebitUseCase) {
        this.findAccountByHolderUseCase = findAccountByHolderUseCase;
        this.recordDebitUseCase = recordDebitUseCase;
    }

    @Override
    public Optional<EntityId> findUnitAccountId(EntityId unitId) {
        return findAccountByHolderUseCase.findAccount(new GetAccountByHolderQuery(unitId, AccountType.UNIT))
                .map(account -> account.id().value());
    }

    @Override
    public boolean propertyAccountExists(EntityId propertyId) {
        return findAccountByHolderUseCase.findAccount(new GetAccountByHolderQuery(propertyId, AccountType.PROPERTY))
                .isPresent();
    }

    @Override
    public void recordDebit(EntityId accountId, BigDecimal amount, String label) {
        recordDebitUseCase.record(new RecordDebitCommand(AccountId.of(accountId.value()), amount, label));
    }
}
