package com.architek.oikos.user.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EmailVO;

/**
 * User's own view of a party's identity fields, decoupled from the party
 * feature's own PartyView (rule 6: cross-feature access only through ports).
 */
public record PartyDetails(String fullName, EmailVO email, String phone) {
}
