package com.architek.oikos.property.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to invite a unit owner's Party to link an AppUser
 * account, right after that Party is resolved/created. Implemented in
 * property.infrastructure.adapter by delegating to user's public port-in
 * use case - never to user's repository directly (rule 6).
 */
public interface AccountLinkingPort {

    void inviteOwnerIfUnlinked(EntityId partyId, EmailVO email, String fullName);
}
