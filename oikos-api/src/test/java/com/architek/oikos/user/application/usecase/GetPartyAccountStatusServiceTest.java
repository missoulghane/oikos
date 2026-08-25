package com.architek.oikos.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyAccountStatus;
import com.architek.oikos.user.application.port.out.OutstandingPartyInvitationPort;
import com.architek.oikos.user.domain.model.PartyInvitationToken;
import com.architek.oikos.user.domain.repository.PartyInvitationTokenRepository;
import com.architek.oikos.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetPartyAccountStatusServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-21T10:00:00Z");

    @Mock
    private UserRepository userRepository;

    @Mock
    private PartyInvitationTokenRepository partyInvitationTokenRepository;

    @Mock
    private OutstandingPartyInvitationPort outstandingPartyInvitationPort;

    private GetPartyAccountStatusService newService() {
        return new GetPartyAccountStatusService(userRepository, partyInvitationTokenRepository,
                outstandingPartyInvitationPort, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static PartyInvitationToken tokenExpiringAt(EntityId partyId, Instant expiresAt) {
        return PartyInvitationToken.issue(partyId, EmailVO.of("jane.doe@example.com"), "Jane Doe", "raw-token", expiresAt);
    }

    @Test
    void a_party_linked_to_an_account_is_active() {
        EntityId partyId = EntityId.newId();
        when(userRepository.existsByLinkedPartyId(partyId)).thenReturn(true);

        assertThat(newService().statusOf(partyId)).isEqualTo(PartyAccountStatus.ACTIVE);
    }

    /**
     * La source normale depuis que l'invitation part de la fiche du contact et
     * porte sur un lot : c'est elle qui fait dire « une invitation est en
     * cours » (voir OutstandingPartyInvitationPort).
     */
    @Test
    void an_unlinked_party_with_an_outstanding_lot_invitation_is_invited() {
        EntityId partyId = EntityId.newId();
        when(userRepository.existsByLinkedPartyId(partyId)).thenReturn(false);
        when(outstandingPartyInvitationPort.existsFor(partyId)).thenReturn(true);

        assertThat(newService().statusOf(partyId)).isEqualTo(PartyAccountStatus.INVITED);
    }

    /** Le compte l'emporte : une invitation qui court encore ne rend pas un contact déjà rattaché « invité ». */
    @Test
    void a_linked_party_stays_active_even_with_an_outstanding_invitation() {
        EntityId partyId = EntityId.newId();
        when(userRepository.existsByLinkedPartyId(partyId)).thenReturn(true);
        when(outstandingPartyInvitationPort.existsFor(partyId)).thenReturn(true);

        assertThat(newService().statusOf(partyId)).isEqualTo(PartyAccountStatus.ACTIVE);
    }

    /**
     * L'ancien jeton d'invitation de compte n'est plus émis par l'interface,
     * mais ceux partis avant ce changement courent jusqu'à leur expiration :
     * les ignorer ferait disparaître un bandeau encore vrai.
     */
    @Test
    void an_unlinked_party_with_a_live_invitation_is_invited() {
        EntityId partyId = EntityId.newId();
        when(userRepository.existsByLinkedPartyId(partyId)).thenReturn(false);
        when(partyInvitationTokenRepository.findByPartyId(partyId))
                .thenReturn(Optional.of(tokenExpiringAt(partyId, NOW.plusSeconds(3600))));

        assertThat(newService().statusOf(partyId)).isEqualTo(PartyAccountStatus.INVITED);
    }

    @Test
    void an_expired_invitation_is_no_longer_in_progress() {
        EntityId partyId = EntityId.newId();
        when(userRepository.existsByLinkedPartyId(partyId)).thenReturn(false);
        when(outstandingPartyInvitationPort.existsFor(partyId)).thenReturn(false);
        when(partyInvitationTokenRepository.findByPartyId(partyId))
                .thenReturn(Optional.of(tokenExpiringAt(partyId, NOW.minusSeconds(1))));

        assertThat(newService().statusOf(partyId)).isEqualTo(PartyAccountStatus.NONE);
    }

    @Test
    void a_party_with_neither_account_nor_invitation_has_no_status_to_speak_of() {
        EntityId partyId = EntityId.newId();
        when(userRepository.existsByLinkedPartyId(partyId)).thenReturn(false);
        when(outstandingPartyInvitationPort.existsFor(partyId)).thenReturn(false);
        when(partyInvitationTokenRepository.findByPartyId(partyId)).thenReturn(Optional.empty());

        assertThat(newService().statusOf(partyId)).isEqualTo(PartyAccountStatus.NONE);
    }
}
