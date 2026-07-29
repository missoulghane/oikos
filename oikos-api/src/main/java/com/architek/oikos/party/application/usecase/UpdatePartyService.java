package com.architek.oikos.party.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.party.application.command.UpdatePartyCommand;
import com.architek.oikos.party.application.dto.PartyView;
import com.architek.oikos.party.application.port.in.UpdatePartyUseCase;
import com.architek.oikos.party.domain.exception.PartyNotFoundException;
import com.architek.oikos.party.domain.exception.EmailAlreadyUsedException;
import com.architek.oikos.party.domain.exception.PhoneAlreadyUsedException;
import com.architek.oikos.party.domain.model.Party;
import com.architek.oikos.party.domain.repository.PartyRepository;

@Component
public class UpdatePartyService implements UpdatePartyUseCase {

    private final PartyRepository partyRepository;

    public UpdatePartyService(PartyRepository partyRepository) {
        this.partyRepository = partyRepository;
    }

    @Override
    @Transactional
    public PartyView update(UpdatePartyCommand command) {
        Party party = partyRepository.findById(command.id())
                .orElseThrow(() -> new PartyNotFoundException(command.id()));
        if (!party.getEmail().equals(command.email())
                && partyRepository.existsByPropertyIdAndEmail(party.getPropertyId(), command.email())) {
            throw new EmailAlreadyUsedException(command.email().value());
        }
        if (command.phone() != null && !command.phone().isBlank() && !command.phone().equals(party.getPhone())
                && partyRepository.existsByPropertyIdAndPhone(party.getPropertyId(), command.phone())) {
            throw new PhoneAlreadyUsedException(command.phone());
        }
        Party updated = partyRepository.save(
                party.withPartyInfo(command.fullName(), command.partyType(), command.email(), command.phone()));
        return PartyView.from(updated);
    }
}
