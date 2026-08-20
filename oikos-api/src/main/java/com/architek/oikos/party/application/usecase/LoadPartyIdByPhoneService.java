package com.architek.oikos.party.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.party.application.port.in.LoadPartyIdByPhoneUseCase;
import com.architek.oikos.party.domain.repository.PartyRepository;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class LoadPartyIdByPhoneService implements LoadPartyIdByPhoneUseCase {

    private final PartyRepository partyRepository;

    public LoadPartyIdByPhoneService(PartyRepository partyRepository) {
        this.partyRepository = partyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PartyId> loadByPhone(EntityId propertyId, String phone) {
        if (phone == null || phone.isBlank()) {
            return Optional.empty();
        }
        return partyRepository.findByPropertyIdAndPhone(propertyId, phone).map(party -> party.getId());
    }
}
