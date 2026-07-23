package com.architek.oikos.party.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.party.application.port.in.LoadPartyIdByPhoneUseCase;
import com.architek.oikos.party.domain.repository.PartyRepository;
import com.architek.oikos.party.domain.valueobject.PartyId;

@Component
public class LoadPartyIdByPhoneService implements LoadPartyIdByPhoneUseCase {

    private final PartyRepository partyRepository;

    public LoadPartyIdByPhoneService(PartyRepository partyRepository) {
        this.partyRepository = partyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PartyId> loadByPhone(String phone) {
        return partyRepository.findByPhone(phone).map(party -> party.getId());
    }
}
