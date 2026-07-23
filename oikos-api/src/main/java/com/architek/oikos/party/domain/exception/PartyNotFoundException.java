package com.architek.oikos.contact.domain.exception;

import com.architek.oikos.contact.domain.valueobject.ContactId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class ContactNotFoundException extends ResourceNotFoundException {

    public ContactNotFoundException(ContactId id) {
        super("Contact not found with id: " + id);
    }
}
