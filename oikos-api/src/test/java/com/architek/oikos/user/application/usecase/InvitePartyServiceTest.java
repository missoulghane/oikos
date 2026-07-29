package com.architek.oikos.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.command.InvitePartyCommand;
import com.architek.oikos.user.domain.repository.PartyInvitationTokenRepository;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.service.PartyInvitationTokenGenerator;

@ExtendWith(MockitoExtension.class)
class InvitePartyServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PartyInvitationTokenRepository partyInvitationTokenRepository;

    @Mock
    private EmailSenderPort emailSenderPort;

    private InvitePartyService newService() {
        return new InvitePartyService(userRepository, partyInvitationTokenRepository, emailSenderPort,
                new PartyInvitationTokenGenerator(), new PartyInvitationEmailComposer("http://localhost/accept-invitation"),
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), 168L);
    }

    @Test
    void unlinked_party_gets_an_invitation_token_and_an_email() {
        EntityId partyId = EntityId.newId();
        when(userRepository.existsByLinkedPartyId(partyId)).thenReturn(false);

        boolean invited = newService().invite(new InvitePartyCommand(partyId, EmailVO.of("jane.doe@example.com"), "Jane Doe"));

        assertThat(invited).isTrue();
        verify(partyInvitationTokenRepository).deleteByPartyId(partyId);
        verify(partyInvitationTokenRepository).save(any());
        verify(emailSenderPort).send(any(), any(), any());
    }

    @Test
    void already_linked_party_is_left_untouched() {
        EntityId partyId = EntityId.newId();
        when(userRepository.existsByLinkedPartyId(partyId)).thenReturn(true);

        boolean invited = newService().invite(new InvitePartyCommand(partyId, EmailVO.of("jane.doe@example.com"), "Jane Doe"));

        assertThat(invited).isFalse();
        verify(partyInvitationTokenRepository, never()).save(any());
        verify(emailSenderPort, never()).send(any(), any(), any());
    }
}
