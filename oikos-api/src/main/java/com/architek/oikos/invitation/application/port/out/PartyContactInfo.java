package com.architek.oikos.invitation.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Un contact tel qu'invitation a besoin de le lire pour lui adresser une
 * invitation privée : à quelle copropriété il appartient (jamais de confiance
 * dans un identifiant venu du client), comment le nommer, et où lui écrire.
 * {@code email} est nul pour un contact qui n'a qu'un téléphone (voir Party).
 */
public record PartyContactInfo(EntityId propertyId, String fullName, EmailVO email) {
}
