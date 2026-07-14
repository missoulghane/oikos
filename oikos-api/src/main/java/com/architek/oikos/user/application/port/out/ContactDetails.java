package com.architek.oikos.user.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EmailVO;

/**
 * User's own view of a contact's identity fields, decoupled from the contact
 * feature's own ContactView (rule 6: cross-feature access only through ports).
 */
public record ContactDetails(String lastName, String firstName, EmailVO email, String phone) {
}
