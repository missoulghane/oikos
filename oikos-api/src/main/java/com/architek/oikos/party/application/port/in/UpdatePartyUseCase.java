package com.architek.oikos.contact.application.port.in;

import com.architek.oikos.contact.application.command.UpdateContactCommand;
import com.architek.oikos.contact.application.dto.ContactView;

public interface UpdateContactUseCase {

    ContactView update(UpdateContactCommand command);
}
