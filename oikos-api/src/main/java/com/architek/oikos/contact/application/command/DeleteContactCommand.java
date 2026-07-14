package com.architek.oikos.contact.application.command;

import com.architek.oikos.contact.domain.valueobject.ContactId;

public record DeleteContactCommand(ContactId id) {
}
