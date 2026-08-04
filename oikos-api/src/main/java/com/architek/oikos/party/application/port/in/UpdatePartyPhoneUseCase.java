package com.architek.oikos.party.application.port.in;

import com.architek.oikos.party.application.command.UpdatePartyPhoneCommand;
import com.architek.oikos.party.application.dto.PartyView;

public interface UpdatePartyPhoneUseCase {

    PartyView updatePhone(UpdatePartyPhoneCommand command);
}
