package com.architek.oikos.user.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to create the property-scoped Party representing a
 * newly registered property manager. Named distinctly from the deleted
 * PartyDirectoryPort (which used to back the account's own identity - no
 * longer needed now that AppUser is self-sufficient). Implemented in
 * user.infrastructure.adapter by delegating to party's public port-in use
 * cases - never to party's repository directly (rule 6).
 */
public interface PartyProvisioningPort {

    EntityId createParty(PartyProvisioningDetails details);
}
