package com.architek.oikos.invitation.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.RawPassword;

/**
 * Outbound port used to provision an account for an anonymous invitee, read
 * an already-authenticated caller's identity, and grant the property-scoped
 * role once a unit has been claimed. Implemented in
 * invitation.infrastructure.adapter by delegating to user's public port-in
 * use cases - never to user's repository directly (rule 6).
 */
public interface AccountDirectoryPort {

    /**
     * Creates a brand-new, already-verified account.
     *
     * @throws com.architek.oikos.user.domain.exception.EmailAlreadyUsedException if the email is already taken -
     *         propagated as-is (already maps to 400), the signal the frontend uses to offer logging in instead
     */
    EntityId provisionAccount(EmailVO email, String fullName, RawPassword password);

    AccountInfo getAccountInfo(EntityId userId);

    void grantPropertyRole(EntityId userId, EntityId partyId, EntityId propertyId, String targetRole);
}
