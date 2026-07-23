package com.architek.oikos.party.application.port.in;

import com.architek.oikos.party.application.command.DeletePartyCommand;

public interface DeletePartyUseCase {

    void delete(DeletePartyCommand command);
}
