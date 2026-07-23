package com.architek.oikos.contact.application.command;

import com.architek.oikos.contact.domain.valueobject.ContactId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

public record UpdateContactCommand(ContactId id, String lastName, String firstName, EmailVO email, String phone) {
}
