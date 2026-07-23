package com.architek.oikos.party.application.port.in;

import com.architek.oikos.party.application.command.CreatePartyCommand;
import com.architek.oikos.party.domain.valueobject.PartyId;

public interface CreatePartyUseCase {

    PartyId create(CreatePartyCommand command);
}
