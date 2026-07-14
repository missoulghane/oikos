package com.architek.oikos.contact.application.port.in;

import com.architek.oikos.contact.application.command.CreateContactCommand;
import com.architek.oikos.contact.domain.valueobject.ContactId;

public interface CreateContactUseCase {

    ContactId create(CreateContactCommand command);
}
