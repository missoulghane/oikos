package com.architek.oikos.contact.application.port.in;

import java.util.Optional;

import com.architek.oikos.contact.domain.valueobject.ContactId;

/**
 * Public entry point used by other features (e.g. user, for login-identifier
 * resolution) to resolve a contact id from a phone number. Cross-feature access
 * must go through a port-in use case, never through the contact repository directly.
 */
public interface LoadContactIdByPhoneUseCase {

    Optional<ContactId> loadByPhone(String phone);
}
