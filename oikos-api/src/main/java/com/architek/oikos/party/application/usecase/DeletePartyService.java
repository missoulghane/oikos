package com.architek.oikos.party.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.party.application.command.DeletePartyCommand;
import com.architek.oikos.party.application.port.in.DeletePartyUseCase;
import com.architek.oikos.party.domain.exception.PartyNotFoundException;
import com.architek.oikos.party.domain.repository.PartyRepository;

@Component
public class DeletePartyService implements DeletePartyUseCase {

    private final PartyRepository partyRepository;

    public DeletePartyService(PartyRepository partyRepository) {
        this.partyRepository = partyRepository;
    }

    @Override
    @Transactional
    public void delete(DeletePartyCommand command) {
        if (partyRepository.findById(command.id()).isEmpty()) {
            throw new PartyNotFoundException(command.id());
        }
        partyRepository.deleteById(command.id());
    }
}
