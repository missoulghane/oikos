package com.architek.oikos.party.application.port.in;

import com.architek.oikos.party.application.dto.PartyView;
import com.architek.oikos.party.application.query.GetPartyQuery;

public interface GetPartyUseCase {

    PartyView getParty(GetPartyQuery query);
}
