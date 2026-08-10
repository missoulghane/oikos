package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.AddBankAccountCommand;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;

/**
 * Manually configures a property's bank account (role BANK, number 514100 +
 * increment) - unlike the cash account (ProvisionPropertyCashAccountUseCase,
 * provisioned automatically at property creation), a bank account requires
 * details (RIB, bank name...) only the property manager has, so it is added
 * explicitly, after the fact ("exigence supplementaire": "le ou les comptes
 * de banque sont a configurer apres"). A property may have several BANK
 * accounts (Partie 3); every write use case that posts to a bank account
 * requires the caller to name which one explicitly (see
 * TreasuryAccountResolver), never resolves it as a singleton per property.
 */
public interface AddBankAccountUseCase {

    LedgerAccountId add(AddBankAccountCommand command);
}
