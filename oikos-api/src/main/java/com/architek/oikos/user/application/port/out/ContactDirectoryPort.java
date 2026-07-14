package com.architek.oikos.user.application.port.out;

import java.util.Optional;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to create/read/update the contact (identity record) backing a
 * user account, and to resolve a contact id from an email or phone number for
 * login-identifier resolution. Implemented in user.infrastructure.adapter by
 * delegating to contact's public port-in use cases - never to contact's repository
 * directly (rule 6). The generic {@link EntityId} keeps user.application fully
 * decoupled from contact's own ContactId type.
 */
public interface ContactDirectoryPort {

    EntityId createContact(ContactDetails details);

    ContactDetails getContactById(EntityId contactId);

    ContactDetails updateContact(EntityId contactId, ContactDetails details);

    Optional<EntityId> findIdByEmail(EmailVO email);

    Optional<EntityId> findIdByPhone(String phone);
}
