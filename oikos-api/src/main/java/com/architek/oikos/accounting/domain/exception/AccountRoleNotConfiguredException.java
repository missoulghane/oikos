package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Spec &sect;12 (ROLE_COMPTABLE_NON_PARAMETRE, HTTP 500): a functional role
 * has no LedgerAccount mapped for this property/unit - a tenant
 * provisioning bug, not a client error, hence a plain RuntimeException
 * (falls through to GlobalExceptionHandler's generic 500 handler) rather
 * than BusinessException/ConflictException/ResourceNotFoundException.
 */
public class AccountRoleNotConfiguredException extends RuntimeException {

    public AccountRoleNotConfiguredException(AccountRole role, EntityId propertyId) {
        super("No ledger account configured for role " + role + " on property " + propertyId);
    }

    public AccountRoleNotConfiguredException(AccountRole role, EntityId propertyId, EntityId unitId) {
        super("No ledger account configured for role " + role + " on property " + propertyId + ", unit " + unitId);
    }
}
