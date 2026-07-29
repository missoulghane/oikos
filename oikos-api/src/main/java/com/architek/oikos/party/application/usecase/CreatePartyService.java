package com.architek.oikos.party.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.party.application.command.CreatePartyCommand;
import com.architek.oikos.party.application.port.in.CreatePartyUseCase;
import com.architek.oikos.party.domain.exception.EmailAlreadyUsedException;
import com.architek.oikos.party.domain.exception.PhoneAlreadyUsedException;
import com.architek.oikos.party.domain.model.Party;
import com.architek.oikos.party.domain.repository.PartyRepository;
import com.architek.oikos.party.domain.valueobject.PartyId;

@Component
public class CreatePartyService implements CreatePartyUseCase {

    private final PartyRepository partyRepository;

    public CreatePartyService(PartyRepository partyRepository) {
        this.partyRepository = partyRepository;
    }

    @Override
    @Transactional
    public PartyId create(CreatePartyCommand command) {
        if (partyRepository.existsByPropertyIdAndEmail(command.propertyId(), command.email())) {
            throw new EmailAlreadyUsedException(command.email().value());
        }
        if (command.phone() != null && !command.phone().isBlank()
                && partyRepository.existsByPropertyIdAndPhone(command.propertyId(), command.phone())) {
            throw new PhoneAlreadyUsedException(command.phone());
        }
        Party party = Party.create(PartyId.newId(), command.propertyId(), command.fullName(), command.partyType(),
                command.email(), command.phone());
        return partyRepository.save(party).getId();
    }
}
