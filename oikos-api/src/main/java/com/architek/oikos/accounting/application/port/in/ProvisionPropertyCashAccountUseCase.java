package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Provisions a property's cash ledger account (role CASH, number 516100 +
 * increment) as soon as the property is created ("exigence
 * supplementaire") - bank accounts (role BANK) are configured manually
 * afterwards, not provisioned here.
 */
public interface ProvisionPropertyCashAccountUseCase {

    LedgerAccountId provision(EntityId propertyId);
}
