package com.architek.oikos.user.application.usecase;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.party.domain.exception.EmailAlreadyUsedException;
import com.architek.oikos.party.domain.exception.PhoneAlreadyUsedException;
import com.architek.oikos.party.domain.repository.PartyRepository;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.command.UpdateUserProfileCommand;
import com.architek.oikos.user.application.dto.UserView;
import com.architek.oikos.user.application.port.in.UpdateUserProfileUseCase;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class UpdateUserProfileService implements UpdateUserProfileUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateUserProfileService.class);

    private final UserRepository userRepository;
    private final PartyRepository partyRepository;

    public UpdateUserProfileService(UserRepository userRepository, PartyRepository partyRepository) {
        this.userRepository = userRepository;
        this.partyRepository = partyRepository;
    }

    @Override
    @Transactional
    public UserView updateProfile(UpdateUserProfileCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        if (!user.getEmail().equals(command.email())
                && userRepository.existsByEmail(command.email().value())) {
            throw new com.architek.oikos.user.domain.exception.EmailAlreadyUsedException(command.email().value());
        }
        User updated = userRepository.save(
                user.withEmail(command.email()).withFullName(command.fullName()).withPhone(command.phone()));
        cascadeToLinkedParties(updated);
        return UserView.of(updated);
    }

    /**
     * The account's email/phone now take precedence over every linked Party's own copy
     * (product decision - previously each Party managed its contact info independently,
     * see EditPartyPhoneForm/UpdatePartyService). Best-effort per party: a party's phone/
     * email is only unique within its own property (uk_party_property_email/phone), so a
     * collision on one property must not block saving the account itself or updating the
     * user's other, unrelated parties - that party's copy is simply left stale and logged.
     */
    private void cascadeToLinkedParties(User user) {
        for (EntityId partyId : user.getLinkedPartyIds()) {
            partyRepository.findById(PartyId.of(partyId.value())).ifPresent(party -> {
                if (party.getEmail().equals(user.getEmail())
                        && Objects.equals(party.getPhone(), user.getPhone())) {
                    return;
                }
                try {
                    if (!party.getEmail().equals(user.getEmail())
                            && partyRepository.existsByPropertyIdAndEmail(party.getPropertyId(), user.getEmail())) {
                        throw new EmailAlreadyUsedException(user.getEmail().value());
                    }
                    if (user.getPhone() != null && !user.getPhone().equals(party.getPhone())
                            && partyRepository.existsByPropertyIdAndPhone(party.getPropertyId(), user.getPhone())) {
                        throw new PhoneAlreadyUsedException(user.getPhone());
                    }
                    partyRepository.save(party.withPartyInfo(
                            party.getFullName(), party.getPartyType(), user.getEmail(), user.getPhone()));
                } catch (EmailAlreadyUsedException | PhoneAlreadyUsedException e) {
                    log.warn("Could not cascade account contact info to party {} on property {}: {}",
                            party.getId(), party.getPropertyId(), e.getMessage());
                }
            });
        }
    }
}
