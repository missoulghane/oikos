package com.architek.oikos.invitation.application.port.out;

import java.util.Optional;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to read an already-authenticated caller's identity and
 * grant the property-scoped role once a unit has been claimed. Implemented
 * in invitation.infrastructure.adapter by delegating to user's public
 * port-in use cases - never to user's repository directly (rule 6).
 */
public interface AccountDirectoryPort {

    AccountInfo getAccountInfo(EntityId userId);

    /**
     * L'identité d'un compte qui a pu disparaître. Optional plutôt qu'un
     * try/catch chez l'appelant : l'exception de getAccountInfo traverse le
     * proxy transactionnel de GetUserService et marque la transaction
     * rollback-only, si bien que la rattraper ne sauve rien - le commit échoue
     * ensuite en UnexpectedRollbackException.
     */
    Optional<AccountInfo> findAccountInfo(EntityId userId);

    void grantPropertyRole(EntityId userId, EntityId partyId, EntityId propertyId, String targetRole);
}
