package com.architek.oikos.contact.application.dto;

import com.architek.oikos.contact.domain.model.Contact;
import com.architek.oikos.contact.domain.valueobject.ContactId;

public record ContactView(ContactId id, String lastName, String firstName, String email, String phone) {

    public static ContactView from(Contact contact) {
        return new ContactView(contact.getId(), contact.getLastName(), contact.getFirstName(),
                contact.getEmail().value(), contact.getPhone());
    }
}
