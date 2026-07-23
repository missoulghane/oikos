package com.architek.oikos.party.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.party.application.dto.PartyView;
import com.architek.oikos.party.application.port.in.GetPartyUseCase;
import com.architek.oikos.party.application.query.GetPartyQuery;
import com.architek.oikos.party.domain.exception.PartyNotFoundException;
import com.architek.oikos.party.domain.model.Party;
import com.architek.oikos.party.domain.repository.PartyRepository;

@Component
public class GetPartyService implements GetPartyUseCase {

    private final PartyRepository partyRepository;

    public GetPartyService(PartyRepository partyRepository) {
        this.partyRepository = partyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PartyView getParty(GetPartyQuery query) {
        Party party = partyRepository.findById(query.id())
                .orElseThrow(() -> new PartyNotFoundException(query.id()));
        return PartyView.from(party);
    }
}
