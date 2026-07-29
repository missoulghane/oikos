package com.architek.oikos.user.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * User's own view of the property-scoped Party to create for a newly
 * registered property manager, decoupled from the party feature's own
 * command types (rule 6: cross-feature access only through ports).
 */
public record PartyProvisioningDetails(String fullName, EmailVO email, String phone, EntityId propertyId) {
}
