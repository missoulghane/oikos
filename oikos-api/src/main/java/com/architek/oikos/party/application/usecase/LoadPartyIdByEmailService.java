package com.architek.oikos.party.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.party.application.port.in.LoadPartyIdByEmailUseCase;
import com.architek.oikos.party.domain.repository.PartyRepository;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class LoadPartyIdByEmailService implements LoadPartyIdByEmailUseCase {

    private final PartyRepository partyRepository;

    public LoadPartyIdByEmailService(PartyRepository partyRepository) {
        this.partyRepository = partyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PartyId> loadByEmail(EntityId propertyId, EmailVO email) {
        return partyRepository.findByPropertyIdAndEmail(propertyId, email).map(party -> party.getId());
    }
}
