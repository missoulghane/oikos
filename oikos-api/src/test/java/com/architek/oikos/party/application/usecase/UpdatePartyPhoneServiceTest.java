package com.architek.oikos.party.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.party.application.command.UpdatePartyPhoneCommand;
import com.architek.oikos.party.domain.exception.PartyNotFoundException;
import com.architek.oikos.party.domain.exception.PhoneAlreadyUsedException;
import com.architek.oikos.party.domain.model.Party;
import com.architek.oikos.party.domain.repository.PartyRepository;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

@ExtendWith(MockitoExtension.class)
class UpdatePartyPhoneServiceTest {

    @Mock
    private PartyRepository partyRepository;

    private final EntityId propertyId = EntityId.newId();

    private UpdatePartyPhoneService newService() {
        return new UpdatePartyPhoneService(partyRepository);
    }

    @Test
    void updating_the_phone_persists_it_and_leaves_other_fields_untouched() {
        PartyId id = PartyId.newId();
        Party party = Party.create(id, propertyId, "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), null);
        when(partyRepository.findById(id)).thenReturn(Optional.of(party));
        when(partyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var command = new UpdatePartyPhoneCommand(id, "0700000000");
        var view = newService().updatePhone(command);

        assertThat(view.phone()).isEqualTo("0700000000");
        assertThat(view.fullName()).isEqualTo("Jane Doe");
        assertThat(view.partyType()).isEqualTo(PartyType.INDIVIDUAL);
        assertThat(view.email()).isEqualTo("jane@doe.com");
    }

    @Test
    void updating_to_a_phone_already_used_by_another_party_is_rejected() {
        PartyId id = PartyId.newId();
        Party party = Party.create(id, propertyId, "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), "0600000000");
        when(partyRepository.findById(id)).thenReturn(Optional.of(party));
        when(partyRepository.existsByPropertyIdAndPhone(propertyId, "0700000000")).thenReturn(true);

        var command = new UpdatePartyPhoneCommand(id, "0700000000");

        assertThatThrownBy(() -> newService().updatePhone(command)).isInstanceOf(PhoneAlreadyUsedException.class);
    }

    @Test
    void updating_with_the_same_phone_does_not_trigger_a_uniqueness_check() {
        PartyId id = PartyId.newId();
        Party party = Party.create(id, propertyId, "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), "0600000000");
        when(partyRepository.findById(id)).thenReturn(Optional.of(party));
        when(partyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var command = new UpdatePartyPhoneCommand(id, "0600000000");

        newService().updatePhone(command);
    }

    @Test
    void updating_the_phone_of_a_missing_party_throws() {
        PartyId id = PartyId.newId();
        when(partyRepository.findById(id)).thenReturn(Optional.empty());

        var command = new UpdatePartyPhoneCommand(id, "0700000000");

        assertThatThrownBy(() -> newService().updatePhone(command)).isInstanceOf(PartyNotFoundException.class);
    }
}
