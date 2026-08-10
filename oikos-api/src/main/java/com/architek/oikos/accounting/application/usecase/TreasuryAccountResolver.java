package com.architek.oikos.accounting.application.usecase;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.domain.exception.InvalidTreasuryAccountException;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Resolves and validates a caller-chosen treasury account (Partie 3: a
 * property can have several BANK accounts, so write use cases can no longer
 * resolve "the" bank account via LedgerAccountRepository.findByPropertyIdAndRole,
 * which assumes a single row per (property, role) - it would throw
 * IncorrectResultSizeDataAccessException as soon as a second BANK row
 * exists). Callers must name the exact account; this checks it actually
 * belongs to the property and carries one of the allowed roles.
 */
@Component
public class TreasuryAccountResolver {

    private final LedgerAccountRepository ledgerAccountRepository;

    public TreasuryAccountResolver(LedgerAccountRepository ledgerAccountRepository) {
        this.ledgerAccountRepository = ledgerAccountRepository;
    }

    public LedgerAccount resolve(EntityId propertyId, LedgerAccountId accountId, Set<AccountRole> allowedRoles) {
        LedgerAccount account = ledgerAccountRepository.findById(accountId)
                .orElseThrow(() -> new InvalidTreasuryAccountException(accountId, propertyId));
        boolean belongsToProperty = account.getPropertyId().map(propertyId::equals).orElse(false);
        boolean hasAllowedRole = account.getRole().map(allowedRoles::contains).orElse(false);
        if (!belongsToProperty || !hasAllowedRole) {
            throw new InvalidTreasuryAccountException(accountId, propertyId);
        }
        return account;
    }
}
