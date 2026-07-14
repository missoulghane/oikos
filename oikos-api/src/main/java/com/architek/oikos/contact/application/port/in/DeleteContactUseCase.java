package com.architek.oikos.contact.application.port.in;

import com.architek.oikos.contact.application.command.DeleteContactCommand;

public interface DeleteContactUseCase {

    void delete(DeleteContactCommand command);
}
