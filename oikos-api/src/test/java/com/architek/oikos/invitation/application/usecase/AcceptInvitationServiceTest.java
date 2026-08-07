package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import com.architek.oikos.invitation.application.port.out.UnitBasicInfo;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.domain.exception.InvalidInvitationTokenException;
import com.architek.oikos.invitation.domain.exception.UnitUnavailableException;
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
class AcceptInvitationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private PartyDirectoryPort partyDirectoryPort;

    @Mock
    private UnitDirectoryPort unitDirectoryPort;

    @Mock
    private AccountDirectoryPort accountDirectoryPort;

    @Mock
    private BoardDirectoryPort boardDirectoryPort;

    @Mock
    private MembershipRequestRepository membershipRequestRepository;

    private AcceptInvitationService newService() {
        return new AcceptInvitationService(invitationRepository, partyDirectoryPort, unitDirectoryPort, accountDirectoryPort,
                boardDirectoryPort, membershipRequestRepository, CLOCK);
    }

    private Invitation privateInvitation(EntityId propertyId) {
        return Invitation.issue(InvitationId.newId(), propertyId, InvitationType.PRIVATE, "PROPERTY_OWNER",
                EmailVO.of("jane.doe@example.com"), "tok", CLOCK.instant().plus(Duration.ofDays(1)), EntityId.newId(), null);
    }

    private Invitation boardInvitation(EntityId propertyId) {
        return Invitation.issue(InvitationId.newId(), propertyId, InvitationType.PRIVATE, "PROPERTY_BOARD_MEMBER",
                EmailVO.of("jane.doe@example.com"), "tok", CLOCK.instant().plus(Duration.ofDays(1)), EntityId.newId(),
                "PRESIDENT");
    }

    @Test
    void accepting_with_a_new_party_creates_it_claims_the_unit_and_grants_the_role() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        Invitation invitation = privateInvitation(propertyId);
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitDirectoryPort.findBasicInfo(unitId))
                .thenReturn(Optional.of(new UnitBasicInfo(propertyId, "A1", "Appartement", true)));

        EntityId actingUserId = EntityId.newId();
        EntityId newPartyId = EntityId.newId();
        when(accountDirectoryPort.getAccountInfo(actingUserId))
                .thenReturn(new AccountInfo(EmailVO.of("jane.doe@example.com"), "Jane Doe"));
        when(partyDirectoryPort.findIdByEmail(EmailVO.of("jane.doe@example.com"), propertyId)).thenReturn(Optional.empty());
        when(partyDirectoryPort.createParty(any(), eq(propertyId))).thenReturn(newPartyId);

        EntityId result = newService().accept(new AcceptInvitationCommand("tok", actingUserId, unitId));

        assertThat(result).isEqualTo(actingUserId);
        verify(unitDirectoryPort).claim(unitId, newPartyId);
        verify(accountDirectoryPort).grantPropertyRole(actingUserId, newPartyId, propertyId, "PROPERTY_OWNER");
        ArgumentCaptor<Invitation> captor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InvitationStatus.CONSUMED);
        assertThat(captor.getValue().getConsumedEmail()).isEqualTo(EmailVO.of("jane.doe@example.com"));
        assertThat(captor.getValue().emailMismatch()).isFalse();

        ArgumentCaptor<MembershipRequest> traceCaptor = ArgumentCaptor.forClass(MembershipRequest.class);
        verify(membershipRequestRepository).save(traceCaptor.capture());
        assertThat(traceCaptor.getValue().getStatus()).isEqualTo(MembershipRequestStatus.ACCEPTED);
        assertThat(traceCaptor.getValue().getUnitId()).isEqualTo(unitId);
        assertThat(traceCaptor.getValue().getPartyId()).isEqualTo(newPartyId);
        assertThat(traceCaptor.getValue().getUserId()).isEqualTo(actingUserId);
        assertThat(traceCaptor.getValue().getDecidedByUserId()).isNull();
    }

    @Test
    void accepting_with_a_mismatched_email_still_grants_access_but_flags_the_invitation() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        Invitation invitation = privateInvitation(propertyId);
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitDirectoryPort.findBasicInfo(unitId))
                .thenReturn(Optional.of(new UnitBasicInfo(propertyId, "A1", "Appartement", true)));

        EntityId actingUserId = EntityId.newId();
        EntityId existingPartyId = EntityId.newId();
        when(accountDirectoryPort.getAccountInfo(actingUserId))
                .thenReturn(new AccountInfo(EmailVO.of("someone.else@example.com"), "Someone Else"));
        when(partyDirectoryPort.findIdByEmail(EmailVO.of("someone.else@example.com"), propertyId))
                .thenReturn(Optional.of(existingPartyId));

        newService().accept(new AcceptInvitationCommand("tok", actingUserId, unitId));

        ArgumentCaptor<Invitation> captor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(captor.capture());
        assertThat(captor.getValue().emailMismatch()).isTrue();
        verify(membershipRequestRepository).save(any());
    }

    @Test
    void accepting_reuses_an_existing_party_matching_the_caller_s_email() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        Invitation invitation = privateInvitation(propertyId);
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitDirectoryPort.findBasicInfo(unitId))
                .thenReturn(Optional.of(new UnitBasicInfo(propertyId, "A1", "Appartement", true)));

        EntityId actingUserId = EntityId.newId();
        EntityId existingPartyId = EntityId.newId();
        when(accountDirectoryPort.getAccountInfo(actingUserId))
                .thenReturn(new AccountInfo(EmailVO.of("jane.doe@example.com"), "Jane Doe"));
        when(partyDirectoryPort.findIdByEmail(EmailVO.of("jane.doe@example.com"), propertyId))
                .thenReturn(Optional.of(existingPartyId));

        EntityId result = newService().accept(new AcceptInvitationCommand("tok", actingUserId, unitId));

        assertThat(result).isEqualTo(actingUserId);
        verify(partyDirectoryPort, never()).createParty(any(), any());
        verify(unitDirectoryPort).claim(unitId, existingPartyId);
        verify(membershipRequestRepository).save(any());
    }

    @Test
    void accepting_a_board_invitation_creates_a_pending_board_member_without_granting_the_role_or_claiming_a_unit() {
        EntityId propertyId = EntityId.newId();
        Invitation invitation = boardInvitation(propertyId);
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EntityId actingUserId = EntityId.newId();
        EntityId newPartyId = EntityId.newId();
        when(accountDirectoryPort.getAccountInfo(actingUserId))
                .thenReturn(new AccountInfo(EmailVO.of("jane.doe@example.com"), "Jane Doe"));
        when(partyDirectoryPort.findIdByEmail(EmailVO.of("jane.doe@example.com"), propertyId)).thenReturn(Optional.empty());
        when(partyDirectoryPort.createParty(any(), eq(propertyId))).thenReturn(newPartyId);

        EntityId result = newService().accept(new AcceptInvitationCommand("tok", actingUserId, null));

        assertThat(result).isEqualTo(actingUserId);
        verify(accountDirectoryPort, never()).grantPropertyRole(any(), any(), any(), any());
        verify(boardDirectoryPort).addPendingBoardMember(propertyId, newPartyId, actingUserId, "PRESIDENT");
        verify(unitDirectoryPort, never()).claim(any(), any());
        verify(membershipRequestRepository, never()).save(any());
        ArgumentCaptor<Invitation> captor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InvitationStatus.CONSUMED);
    }

    @Test
    void accepting_an_invalid_token_is_rejected() {
        when(invitationRepository.findByToken("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().accept(new AcceptInvitationCommand("bad", EntityId.newId(), EntityId.newId())))
                .isInstanceOf(InvalidInvitationTokenException.class);
        verify(membershipRequestRepository, never()).save(any());
    }

    @Test
    void accepting_a_public_invitation_through_this_use_case_is_rejected() {
        Invitation invitation = Invitation.issue(InvitationId.newId(), EntityId.newId(), InvitationType.PUBLIC,
                "PROPERTY_OWNER", null, "tok", CLOCK.instant().plus(Duration.ofDays(1)), EntityId.newId(), null);
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> newService().accept(new AcceptInvitationCommand("tok", EntityId.newId(), EntityId.newId())))
                .isInstanceOf(InvalidInvitationTokenException.class);
        verify(membershipRequestRepository, never()).save(any());
    }

    @Test
    void losing_the_claim_race_disables_the_invitation_and_does_not_consume_it() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        Invitation invitation = privateInvitation(propertyId);
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitDirectoryPort.findBasicInfo(unitId))
                .thenReturn(Optional.of(new UnitBasicInfo(propertyId, "A1", "Appartement", true)));

        EntityId actingUserId = EntityId.newId();
        EntityId newPartyId = EntityId.newId();
        when(accountDirectoryPort.getAccountInfo(actingUserId))
                .thenReturn(new AccountInfo(EmailVO.of("jane.doe@example.com"), "Jane Doe"));
        when(partyDirectoryPort.findIdByEmail(any(), eq(propertyId))).thenReturn(Optional.empty());
        when(partyDirectoryPort.createParty(any(), eq(propertyId))).thenReturn(newPartyId);
        org.mockito.Mockito.doThrow(new UnitUnavailableException("already claimed"))
                .when(unitDirectoryPort).claim(unitId, newPartyId);

        assertThatThrownBy(() -> newService().accept(new AcceptInvitationCommand("tok", actingUserId, unitId)))
                .isInstanceOf(UnitUnavailableException.class);

        ArgumentCaptor<Invitation> captor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InvitationStatus.DISABLED);
        verify(accountDirectoryPort, never()).grantPropertyRole(any(), any(), any(), any());
        verify(membershipRequestRepository, never()).save(any());
    }

    @Test
    void accepting_without_a_unit_id_is_rejected() {
        Invitation invitation = privateInvitation(EntityId.newId());
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> newService().accept(new AcceptInvitationCommand("tok", EntityId.newId(), null)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(membershipRequestRepository, never()).save(any());
    }

    @Test
    void accepting_with_a_unit_from_another_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        EntityId otherPropertyId = EntityId.newId();
        EntityId chosenUnitId = EntityId.newId();
        Invitation invitation = privateInvitation(propertyId);
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(unitDirectoryPort.findBasicInfo(chosenUnitId))
                .thenReturn(Optional.of(new UnitBasicInfo(otherPropertyId, "A1", "Appartement", true)));

        assertThatThrownBy(() -> newService().accept(new AcceptInvitationCommand("tok", EntityId.newId(), chosenUnitId)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(membershipRequestRepository, never()).save(any());
    }
}
