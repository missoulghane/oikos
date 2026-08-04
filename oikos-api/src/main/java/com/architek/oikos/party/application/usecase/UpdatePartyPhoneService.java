package com.architek.oikos.party.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.party.application.command.UpdatePartyPhoneCommand;
import com.architek.oikos.party.application.dto.PartyView;
import com.architek.oikos.party.application.port.in.UpdatePartyPhoneUseCase;
import com.architek.oikos.party.domain.exception.PartyNotFoundException;
import com.architek.oikos.party.domain.exception.PhoneAlreadyUsedException;
import com.architek.oikos.party.domain.model.Party;
import com.architek.oikos.party.domain.repository.PartyRepository;

/**
 * Self-service counterpart to {@link UpdatePartyService}: deliberately narrower than the full
 * party edit (fullName/partyType/email stay admin/manager-only via PUT /parties/{id}) so that
 * the owner-facing "my lot" screen (gated by ownsParty, see PropertyController) can let an owner
 * keep their phone number current without also exposing identity fields.
 */
@Component
public class UpdatePartyPhoneService implements UpdatePartyPhoneUseCase {

    private final PartyRepository partyRepository;

    public UpdatePartyPhoneService(PartyRepository partyRepository) {
        this.partyRepository = partyRepository;
    }

    @Override
    @Transactional
    public PartyView updatePhone(UpdatePartyPhoneCommand command) {
        Party party = partyRepository.findById(command.id())
                .orElseThrow(() -> new PartyNotFoundException(command.id()));
        if (command.phone() != null && !command.phone().isBlank() && !command.phone().equals(party.getPhone())
                && partyRepository.existsByPropertyIdAndPhone(party.getPropertyId(), command.phone())) {
            throw new PhoneAlreadyUsedException(command.phone());
        }
        Party updated = partyRepository.save(
                party.withPartyInfo(party.getFullName(), party.getPartyType(), party.getEmail(), command.phone()));
        return PartyView.from(updated);
    }
}
