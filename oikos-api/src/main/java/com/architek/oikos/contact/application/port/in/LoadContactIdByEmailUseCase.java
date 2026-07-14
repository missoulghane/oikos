package com.architek.oikos.contact.application.port.in;

import java.util.Optional;

import com.architek.oikos.contact.domain.valueobject.ContactId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

/**
 * Public entry point used by other features (e.g. user, for login-identifier
 * resolution) to resolve a contact id from an email. Cross-feature access must go
 * through a port-in use case, never through the contact repository directly.
 */
public interface LoadContactIdByEmailUseCase {

    Optional<ContactId> loadByEmail(EmailVO email);
}
