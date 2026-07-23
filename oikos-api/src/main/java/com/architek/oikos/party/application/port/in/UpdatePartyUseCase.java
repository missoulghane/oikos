package com.architek.oikos.party.application.port.in;

import com.architek.oikos.party.application.command.UpdatePartyCommand;
import com.architek.oikos.party.application.dto.PartyView;

public interface UpdatePartyUseCase {

    PartyView update(UpdatePartyCommand command);
}
