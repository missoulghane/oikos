package com.architek.oikos.party.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.party.application.command.CreatePartyCommand;
import com.architek.oikos.party.domain.exception.EmailAlreadyUsedException;
import com.architek.oikos.party.domain.exception.PhoneAlreadyUsedException;
import com.architek.oikos.party.domain.model.Party;
import com.architek.oikos.party.application.port.out.AccountInvitationPort;
import com.architek.oikos.party.domain.repository.PartyRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

@ExtendWith(MockitoExtension.class)
class CreatePartyServiceTest {

    @Mock
    private PartyRepository partyRepository;

    @Mock
    private AccountInvitationPort accountInvitationPort;

    private final EntityId propertyId = EntityId.newId();

    private CreatePartyService newService() {
        return new CreatePartyService(partyRepository, accountInvitationPort);
    }

    @Test
    void creating_a_party_persists_it() {
        when(partyRepository.existsByPropertyIdAndEmail(any(), any())).thenReturn(false);
        when(partyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreatePartyCommand command = new CreatePartyCommand(propertyId, "Jane Doe", PartyType.INDIVIDUAL,
                EmailVO.of("jane@doe.com"), null, false);

        newService().create(command);

        ArgumentCaptor<Party> captor = ArgumentCaptor.forClass(Party.class);
        verify(partyRepository).save(captor.capture());
        assertThat(captor.getValue().getFullName()).isEqualTo("Jane Doe");
        assertThat(captor.getValue().getPartyType()).isEqualTo(PartyType.INDIVIDUAL);
        assertThat(captor.getValue().getPropertyId()).isEqualTo(propertyId);
    }

    @Test
    void creating_a_party_with_an_already_used_email_is_rejected() {
        when(partyRepository.existsByPropertyIdAndEmail(any(), any())).thenReturn(true);

        CreatePartyCommand command = new CreatePartyCommand(propertyId, "Jane Doe", PartyType.INDIVIDUAL,
                EmailVO.of("jane@doe.com"), null, false);

        assertThatThrownBy(() -> newService().create(command)).isInstanceOf(EmailAlreadyUsedException.class);
    }

    @Test
    void creating_a_party_with_an_already_used_phone_is_rejected() {
        when(partyRepository.existsByPropertyIdAndEmail(any(), any())).thenReturn(false);
        when(partyRepository.existsByPropertyIdAndPhone(propertyId, "0600000000")).thenReturn(true);

        CreatePartyCommand command = new CreatePartyCommand(propertyId, "Jane Doe", PartyType.INDIVIDUAL,
                EmailVO.of("jane@doe.com"), "0600000000", false);

        assertThatThrownBy(() -> newService().create(command)).isInstanceOf(PhoneAlreadyUsedException.class);
    }

    @Test
    void creating_a_party_without_a_phone_never_checks_phone_uniqueness() {
        when(partyRepository.existsByPropertyIdAndEmail(any(), any())).thenReturn(false);
        when(partyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreatePartyCommand command = new CreatePartyCommand(propertyId, "Jane Doe", PartyType.INDIVIDUAL,
                EmailVO.of("jane@doe.com"), null, false);

        newService().create(command);

        verify(partyRepository, never()).existsByPropertyIdAndPhone(any(), any());
    }

    @Test
    void an_invitation_goes_out_when_the_syndic_asked_for_one() {
        when(partyRepository.existsByPropertyIdAndEmail(any(), any())).thenReturn(false);
        when(partyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().create(new CreatePartyCommand(propertyId, "Jane Doe", PartyType.INDIVIDUAL,
                EmailVO.of("jane@doe.com"), null, true));

        verify(accountInvitationPort).inviteIfUnlinked(any(), eq(EmailVO.of("jane@doe.com")), eq("Jane Doe"));
    }

    @Test
    void nothing_is_sent_when_the_box_is_unchecked() {
        // Un gardien, un prestataire : le syndic enregistre aussi des contacts qui
        // n'ont rien à faire dans l'application.
        when(partyRepository.existsByPropertyIdAndEmail(any(), any())).thenReturn(false);
        when(partyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().create(new CreatePartyCommand(propertyId, "Jane Doe", PartyType.INDIVIDUAL,
                EmailVO.of("jane@doe.com"), null, false));

        verify(accountInvitationPort, never()).inviteIfUnlinked(any(), any(), any());
    }

    @Test
    void a_refused_creation_invites_nobody() {
        // L'email est déjà pris : la fiche n'est pas créée, et le porteur de
        // l'adresse ne doit pas recevoir d'invitation pour autant.
        when(partyRepository.existsByPropertyIdAndEmail(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> newService().create(new CreatePartyCommand(propertyId, "Jane Doe",
                PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), null, true)))
                .isInstanceOf(EmailAlreadyUsedException.class);

        verify(accountInvitationPort, never()).inviteIfUnlinked(any(), any(), any());
    }
}
