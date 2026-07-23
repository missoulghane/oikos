package com.architek.oikos.property.application.port.out;

import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

/**
 * Property's own view of a party's identity fields, decoupled from the party
 * feature's own PartyView (rule 6: cross-feature access only through ports).
 */
public record PartyDetails(String fullName, PartyType partyType, EmailVO email) {
}
