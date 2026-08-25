package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.invitation.application.command.AcceptInvitationCommand;
import com.architek.oikos.invitation.application.port.out.AccountDirectoryPort;
import com.architek.oikos.invitation.application.port.out.AccountInfo;
import com.architek.oikos.invitation.application.port.out.BoardDirectoryPort;
import com.architek.oikos.invitation.application.port.out.PartyDirectoryPort;
import com.architek.oikos.invitation.domain.exception.InvalidInvitationTokenException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationStatus;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Ce cas d'usage ne traite plus que les invitations au conseil syndical. Les
 * invitations de copropriétaire, publiques comme privées, déposent désormais
 * une demande que le syndic valide (SubmitMembershipRequestService) - voir le
 * javadoc du service pour la raison.
 */
@ExtendWith(MockitoExtension.class)
class AcceptInvitationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private PartyDirectoryPort partyDirectoryPort;

    @Mock
    private AccountDirectoryPort accountDirectoryPort;

    @Mock
    private BoardDirectoryPort boardDirectoryPort;

    private AcceptInvitationService newService() {
        return new AcceptInvitationService(invitationRepository, partyDirectoryPort, accountDirectoryPort,
                boardDirectoryPort, CLOCK);
    }

    private Invitation boardInvitation(EntityId propertyId) {
        return Invitation.issue(InvitationId.newId(), propertyId, InvitationType.PRIVATE, "PROPERTY_BOARD_MEMBER",
                EmailVO.of("jane.doe@example.com"), "tok", CLOCK.instant().plus(Duration.ofDays(1)), EntityId.newId(),
                "PRESIDENT", null, null);
    }

    private Invitation ownerInvitation(EntityId propertyId, InvitationType type) {
        return Invitation.issue(InvitationId.newId(), propertyId, type, "PROPERTY_OWNER",
                type == InvitationType.PRIVATE ? EmailVO.of("jane.doe@example.com") : null, "tok",
                CLOCK.instant().plus(Duration.ofDays(1)), EntityId.newId(), null,
                type == InvitationType.PRIVATE ? EntityId.newId() : null, null);
    }

    @Test
    void accepting_a_board_invitation_creates_a_pending_board_member_without_granting_the_role() {
        EntityId propertyId = EntityId.newId();
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(boardInvitation(propertyId)));
        when(invitationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EntityId actingUserId = EntityId.newId();
        EntityId newPartyId = EntityId.newId();
        when(accountDirectoryPort.getAccountInfo(actingUserId))
                .thenReturn(new AccountInfo(EmailVO.of("jane.doe@example.com"), "Jane Doe", "212600000000", true));
        when(partyDirectoryPort.findIdByEmail(EmailVO.of("jane.doe@example.com"), propertyId)).thenReturn(Optional.empty());
        when(partyDirectoryPort.createParty(any(), eq(propertyId))).thenReturn(newPartyId);

        EntityId result = newService().accept(new AcceptInvitationCommand("tok", actingUserId, null));

        assertThat(result).isEqualTo(actingUserId);
        verify(accountDirectoryPort, never()).grantPropertyRole(any(), any(), any(), any());
        verify(boardDirectoryPort).addPendingBoardMember(propertyId, newPartyId, actingUserId, "PRESIDENT");
        ArgumentCaptor<Invitation> captor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InvitationStatus.CONSUMED);
    }

    @Test
    void accepting_reuses_an_existing_party_matching_the_caller_s_email() {
        EntityId propertyId = EntityId.newId();
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(boardInvitation(propertyId)));
        when(invitationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EntityId actingUserId = EntityId.newId();
        EntityId existingPartyId = EntityId.newId();
        when(accountDirectoryPort.getAccountInfo(actingUserId))
                .thenReturn(new AccountInfo(EmailVO.of("jane.doe@example.com"), "Jane Doe", "212600000000", true));
        when(partyDirectoryPort.findIdByEmail(EmailVO.of("jane.doe@example.com"), propertyId))
                .thenReturn(Optional.of(existingPartyId));

        newService().accept(new AcceptInvitationCommand("tok", actingUserId, null));

        verify(partyDirectoryPort, never()).createParty(any(), any());
        verify(boardDirectoryPort).addPendingBoardMember(propertyId, existingPartyId, actingUserId, "PRESIDENT");
    }

    @Test
    void accepting_an_invalid_token_is_rejected() {
        when(invitationRepository.findByToken("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().accept(new AcceptInvitationCommand("bad", EntityId.newId(), null)))
                .isInstanceOf(InvalidInvitationTokenException.class);
        verify(boardDirectoryPort, never()).addPendingBoardMember(any(), any(), any(), any());
    }

    @Test
    void accepting_a_disabled_invitation_is_rejected() {
        when(invitationRepository.findByToken("tok"))
                .thenReturn(Optional.of(boardInvitation(EntityId.newId()).disable()));

        assertThatThrownBy(() -> newService().accept(new AcceptInvitationCommand("tok", EntityId.newId(), null)))
                .isInstanceOf(InvalidInvitationTokenException.class);
        verify(boardDirectoryPort, never()).addPendingBoardMember(any(), any(), any(), any());
    }

    /**
     * L'ancien chemin accordait ici l'accès sur-le-champ, en réservant le lot
     * choisi par l'invité. C'est précisément la porte que la page d'accueil
     * ouvre au changement de lot : sans revue du syndic, n'importe quel invité
     * s'attribuerait le lot de son choix. Le refus est explicite plutôt que
     * silencieux - un client resté sur l'ancien appel doit le savoir.
     */
    @Test
    void a_private_owner_invitation_can_no_longer_be_accepted_directly() {
        when(invitationRepository.findByToken("tok"))
                .thenReturn(Optional.of(ownerInvitation(EntityId.newId(), InvitationType.PRIVATE)));

        assertThatThrownBy(() -> newService().accept(
                new AcceptInvitationCommand("tok", EntityId.newId(), EntityId.newId())))
                .isInstanceOf(InvalidInvitationTokenException.class)
                .hasMessageContaining("membership request");
        verify(invitationRepository, never()).save(any());
        verify(accountDirectoryPort, never()).grantPropertyRole(any(), any(), any(), any());
    }

    @Test
    void accepting_a_public_invitation_through_this_use_case_is_rejected() {
        when(invitationRepository.findByToken("tok"))
                .thenReturn(Optional.of(ownerInvitation(EntityId.newId(), InvitationType.PUBLIC)));

        assertThatThrownBy(() -> newService().accept(
                new AcceptInvitationCommand("tok", EntityId.newId(), EntityId.newId())))
                .isInstanceOf(InvalidInvitationTokenException.class);
        verify(invitationRepository, never()).save(any());
    }
}
