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

import com.architek.oikos.party.application.command.UpdatePartyCommand;
import com.architek.oikos.party.domain.exception.PartyNotFoundException;
import com.architek.oikos.party.domain.exception.EmailAlreadyUsedException;
import com.architek.oikos.party.domain.exception.PhoneAlreadyUsedException;
import com.architek.oikos.party.domain.model.Party;
import com.architek.oikos.party.domain.repository.PartyRepository;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

@ExtendWith(MockitoExtension.class)
class UpdatePartyServiceTest {

    @Mock
    private PartyRepository partyRepository;

    private UpdatePartyService newService() {
        return new UpdatePartyService(partyRepository);
    }

    @Test
    void updating_a_party_persists_the_new_fields() {
        PartyId id = PartyId.newId();
        Party party = Party.create(id, "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), null);
        when(partyRepository.findById(id)).thenReturn(Optional.of(party));
        when(partyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var command = new UpdatePartyCommand(id, "Janet Smith", PartyType.COMPANY, EmailVO.of("janet@smith.com"), "0700000000");
        var view = newService().update(command);

        assertThat(view.fullName()).isEqualTo("Janet Smith");
        assertThat(view.partyType()).isEqualTo(PartyType.COMPANY);
        assertThat(view.email()).isEqualTo("janet@smith.com");
    }

    @Test
    void updating_with_the_same_email_does_not_trigger_a_uniqueness_check() {
        PartyId id = PartyId.newId();
        Party party = Party.create(id, "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), null);
        when(partyRepository.findById(id)).thenReturn(Optional.of(party));
        when(partyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var command = new UpdatePartyCommand(id, "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), "0700000000");

        newService().update(command);
    }

    @Test
    void updating_to_an_email_already_used_by_another_party_is_rejected() {
        PartyId id = PartyId.newId();
        Party party = Party.create(id, "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), null);
        when(partyRepository.findById(id)).thenReturn(Optional.of(party));
        when(partyRepository.existsByEmail(EmailVO.of("taken@doe.com"))).thenReturn(true);

        var command = new UpdatePartyCommand(id, "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("taken@doe.com"), null);

        assertThatThrownBy(() -> newService().update(command)).isInstanceOf(EmailAlreadyUsedException.class);
    }

    @Test
    void updating_to_a_phone_already_used_by_another_party_is_rejected() {
        PartyId id = PartyId.newId();
        Party party = Party.create(id, "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), "0600000000");
        when(partyRepository.findById(id)).thenReturn(Optional.of(party));
        when(partyRepository.existsByPhone("0700000000")).thenReturn(true);

        var command = new UpdatePartyCommand(id, "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), "0700000000");

        assertThatThrownBy(() -> newService().update(command)).isInstanceOf(PhoneAlreadyUsedException.class);
    }

    @Test
    void updating_with_the_same_phone_does_not_trigger_a_uniqueness_check() {
        PartyId id = PartyId.newId();
        Party party = Party.create(id, "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), "0600000000");
        when(partyRepository.findById(id)).thenReturn(Optional.of(party));
        when(partyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var command = new UpdatePartyCommand(id, "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), "0600000000");

        newService().update(command);
    }

    @Test
    void updating_a_missing_party_throws() {
        PartyId id = PartyId.newId();
        when(partyRepository.findById(id)).thenReturn(Optional.empty());

        var command = new UpdatePartyCommand(id, "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), null);

        assertThatThrownBy(() -> newService().update(command)).isInstanceOf(PartyNotFoundException.class);
    }
}
