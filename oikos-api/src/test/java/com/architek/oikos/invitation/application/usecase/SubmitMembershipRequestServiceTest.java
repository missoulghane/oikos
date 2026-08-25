package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.invitation.application.command.SubmitMembershipRequestCommand;
import com.architek.oikos.invitation.application.port.out.AccountDirectoryPort;
import com.architek.oikos.invitation.application.port.out.AccountInfo;
import com.architek.oikos.invitation.application.port.out.BoardStaffDirectoryPort;
import com.architek.oikos.invitation.application.port.out.NotificationPort;
import com.architek.oikos.invitation.application.port.out.PartyDirectoryPort;
import com.architek.oikos.invitation.application.port.out.UnitBasicInfo;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.domain.exception.InvalidInvitationTokenException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationStatus;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.model.MembershipRequestStatus;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class SubmitMembershipRequestServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private MembershipRequestRepository membershipRequestRepository;

    @Mock
    private PartyDirectoryPort partyDirectoryPort;

    @Mock
    private UnitDirectoryPort unitDirectoryPort;

    @Mock
    private AccountDirectoryPort accountDirectoryPort;

    @Mock
    private BoardStaffDirectoryPort boardStaffDirectoryPort;

    @Mock
    private NotificationPort notificationPort;

    private SubmitMembershipRequestService newService() {
        return new SubmitMembershipRequestService(invitationRepository, membershipRequestRepository, partyDirectoryPort,
                unitDirectoryPort, accountDirectoryPort, boardStaffDirectoryPort, notificationPort, CLOCK);
    }

    private Invitation publicInvitation(EntityId propertyId) {
        return Invitation.issue(InvitationId.newId(), propertyId, InvitationType.PUBLIC, "PROPERTY_OWNER", null,
                "tok", CLOCK.instant().plus(Duration.ofDays(30)), EntityId.newId(), null, null, null);
    }

    private Invitation privateInvitation(EntityId propertyId, EntityId targetUnitId, EntityId targetPartyId) {
        return Invitation.issue(InvitationId.newId(), propertyId, InvitationType.PRIVATE, "PROPERTY_OWNER",
                EmailVO.of("jane.doe@example.com"), "tok", CLOCK.instant().plus(Duration.ofDays(30)), EntityId.newId(),
                null, targetUnitId, targetPartyId);
    }

    @Test
    void submitting_a_membership_request_creates_a_pending_request() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        Invitation invitation = publicInvitation(propertyId);
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(unitDirectoryPort.findBasicInfo(unitId)).thenReturn(Optional.of(new UnitBasicInfo(propertyId, "A1", "Appartement", true)));

        EntityId actingUserId = EntityId.newId();
        EntityId newPartyId = EntityId.newId();
        when(accountDirectoryPort.getAccountInfo(actingUserId))
                .thenReturn(new AccountInfo(EmailVO.of("jane.doe@example.com"), "Jane Doe", "212600000000", true));
        when(partyDirectoryPort.findIdByEmail(EmailVO.of("jane.doe@example.com"), propertyId)).thenReturn(Optional.empty());
        when(partyDirectoryPort.createParty(any(), eq(propertyId))).thenReturn(newPartyId);
        when(membershipRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        EntityId boardUserId = EntityId.newId();
        when(boardStaffDirectoryPort.listStaffUserIds(propertyId)).thenReturn(List.of(boardUserId));

        newService().submit(new SubmitMembershipRequestCommand("tok", actingUserId, unitId));

        ArgumentCaptor<MembershipRequest> captor = ArgumentCaptor.forClass(MembershipRequest.class);
        org.mockito.Mockito.verify(membershipRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(MembershipRequestStatus.PENDING);
        assertThat(captor.getValue().getUnitId()).isEqualTo(unitId);
        assertThat(captor.getValue().getPartyId()).isEqualTo(newPartyId);
        assertThat(captor.getValue().getUserId()).isEqualTo(actingUserId);
        // REQUEST_RECEIVED (GAP.md §3.1): every active board seat on the property is notified.
        org.mockito.Mockito.verify(notificationPort).notifyRequestReceived(eq(boardUserId), eq(propertyId), any(), any(), any());
    }

    /**
     * Une invitation privée dépose désormais une demande comme le lien public :
     * son lot n'est qu'un point de départ, et c'est le syndic qui tranche.
     * Le lot vient de l'invitation quand l'invité ne le remplace pas.
     */
    @Test
    void a_private_invitation_falls_back_on_the_lot_it_designates_and_consumes_the_link() {
        EntityId propertyId = EntityId.newId();
        EntityId targetUnitId = EntityId.newId();
        EntityId invitedPartyId = EntityId.newId();
        Invitation invitation = privateInvitation(propertyId, targetUnitId, invitedPartyId);
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(unitDirectoryPort.findBasicInfo(targetUnitId))
                .thenReturn(Optional.of(new UnitBasicInfo(propertyId, "A-12", "Appartement", false)));

        EntityId actingUserId = EntityId.newId();
        when(accountDirectoryPort.getAccountInfo(actingUserId))
                .thenReturn(new AccountInfo(EmailVO.of("jane.doe@example.com"), "Jane Doe", "212600000000", true));
        when(membershipRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().submit(new SubmitMembershipRequestCommand("tok", actingUserId, null));

        ArgumentCaptor<MembershipRequest> captor = ArgumentCaptor.forClass(MembershipRequest.class);
        org.mockito.Mockito.verify(membershipRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(MembershipRequestStatus.PENDING);
        assertThat(captor.getValue().getUnitId()).isEqualTo(targetUnitId);
        assertThat(captor.getValue().getPartyId()).isEqualTo(invitedPartyId);

        // Nominatif et à usage unique : le laisser ACTIVE permettrait à qui l'a
        // reçu par ricochet de déposer une seconde demande.
        ArgumentCaptor<Invitation> linkCaptor = ArgumentCaptor.forClass(Invitation.class);
        org.mockito.Mockito.verify(invitationRepository).save(linkCaptor.capture());
        assertThat(linkCaptor.getValue().getStatus()).isEqualTo(InvitationStatus.CONSUMED);
    }

    /** « Ce n'est pas votre lot ? » : le lot choisi par l'invité l'emporte sur celui de l'invitation. */
    @Test
    void a_private_invitation_lets_the_invitee_designate_another_lot() {
        EntityId propertyId = EntityId.newId();
        EntityId targetUnitId = EntityId.newId();
        EntityId chosenUnitId = EntityId.newId();
        when(invitationRepository.findByToken("tok"))
                .thenReturn(Optional.of(privateInvitation(propertyId, targetUnitId, EntityId.newId())));
        when(unitDirectoryPort.findBasicInfo(chosenUnitId))
                .thenReturn(Optional.of(new UnitBasicInfo(propertyId, "B-03", "Parking", true)));

        EntityId actingUserId = EntityId.newId();
        when(accountDirectoryPort.getAccountInfo(actingUserId))
                .thenReturn(new AccountInfo(EmailVO.of("jane.doe@example.com"), "Jane Doe", null, true));
        when(membershipRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().submit(new SubmitMembershipRequestCommand("tok", actingUserId, chosenUnitId));

        ArgumentCaptor<MembershipRequest> captor = ArgumentCaptor.forClass(MembershipRequest.class);
        org.mockito.Mockito.verify(membershipRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getUnitId()).isEqualTo(chosenUnitId);
    }

    /**
     * L'invitation porte l'identifiant du contact : c'est lui qu'on reprend,
     * même si l'invité crée son compte avec une autre adresse. Sans cela on
     * fabriquerait un second contact pour la même personne, et l'attribution du
     * lot échouerait à la validation - il appartient déjà au premier.
     */
    @Test
    void a_private_invitation_binds_the_request_to_the_invited_contact_even_on_another_account_email() {
        EntityId propertyId = EntityId.newId();
        EntityId targetUnitId = EntityId.newId();
        EntityId invitedPartyId = EntityId.newId();
        when(invitationRepository.findByToken("tok"))
                .thenReturn(Optional.of(privateInvitation(propertyId, targetUnitId, invitedPartyId)));
        when(unitDirectoryPort.findBasicInfo(targetUnitId))
                .thenReturn(Optional.of(new UnitBasicInfo(propertyId, "A-12", "Appartement", false)));

        EntityId actingUserId = EntityId.newId();
        when(accountDirectoryPort.getAccountInfo(actingUserId))
                .thenReturn(new AccountInfo(EmailVO.of("autre.adresse@example.com"), "Jane Doe", null, true));
        when(membershipRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().submit(new SubmitMembershipRequestCommand("tok", actingUserId, null));

        ArgumentCaptor<MembershipRequest> captor = ArgumentCaptor.forClass(MembershipRequest.class);
        org.mockito.Mockito.verify(membershipRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getPartyId()).isEqualTo(invitedPartyId);
        org.mockito.Mockito.verifyNoInteractions(partyDirectoryPort);
    }

    /** Un siège au conseil n'est pas un lot : il reste sur AcceptInvitationService. */
    @Test
    void submitting_a_membership_request_for_a_board_invitation_is_rejected() {
        Invitation invitation = Invitation.issue(InvitationId.newId(), EntityId.newId(), InvitationType.PRIVATE,
                "PROPERTY_BOARD_MEMBER", EmailVO.of("jane.doe@example.com"), "tok",
                CLOCK.instant().plus(Duration.ofDays(30)), EntityId.newId(), "PRESIDENT", null, null);
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> newService().submit(new SubmitMembershipRequestCommand("tok", EntityId.newId(), EntityId.newId())))
                .isInstanceOf(InvalidInvitationTokenException.class);
    }

    @Test
    void submitting_a_membership_request_without_a_unit_id_is_rejected() {
        Invitation invitation = publicInvitation(EntityId.newId());
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> newService().submit(new SubmitMembershipRequestCommand("tok", EntityId.newId(), null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void submitting_the_same_request_twice_is_idempotent() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        EntityId actingUserId = EntityId.newId();
        Invitation invitation = publicInvitation(propertyId);
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));

        MembershipRequest existing = MembershipRequest.submit(com.architek.oikos.invitation.domain.valueobject.MembershipRequestId.newId(),
                EntityId.of(invitation.getId().asUuid()), propertyId, unitId, EntityId.newId(), actingUserId);
        when(membershipRequestRepository.findByInvitationIdAndUnitIdAndUserId(EntityId.of(invitation.getId().asUuid()), unitId, actingUserId))
                .thenReturn(Optional.of(existing));

        var result = newService().submit(new SubmitMembershipRequestCommand("tok", actingUserId, unitId));

        assertThat(result).isEqualTo(existing.getId());
        org.mockito.Mockito.verify(membershipRequestRepository, org.mockito.Mockito.never()).save(any());
        org.mockito.Mockito.verifyNoInteractions(accountDirectoryPort);
    }
}
