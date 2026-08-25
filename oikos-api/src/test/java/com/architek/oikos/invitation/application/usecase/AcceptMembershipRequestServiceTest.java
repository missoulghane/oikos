package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import org.springframework.context.ApplicationEventPublisher;

import com.architek.oikos.invitation.application.command.AcceptMembershipRequestCommand;
import com.architek.oikos.invitation.application.event.MembershipRequestDecidedEvent;
import com.architek.oikos.invitation.application.port.out.AccountDirectoryPort;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.domain.exception.MembershipRequestAlreadyDecidedException;
import com.architek.oikos.invitation.domain.exception.MembershipRequestNotFoundException;
import com.architek.oikos.invitation.domain.exception.UnitUnavailableException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class AcceptMembershipRequestServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);

    @Mock
    private MembershipRequestRepository membershipRequestRepository;

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private UnitDirectoryPort unitDirectoryPort;

    @Mock
    private AccountDirectoryPort accountDirectoryPort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AcceptMembershipRequestService newService() {
        return new AcceptMembershipRequestService(membershipRequestRepository, invitationRepository, unitDirectoryPort,
                accountDirectoryPort, eventPublisher, CLOCK);
    }

    @Test
    void accepting_a_pending_request_claims_the_unit_grants_the_role_and_rejects_siblings() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        EntityId partyId = EntityId.newId();
        EntityId userId = EntityId.newId();
        EntityId decidedByUserId = EntityId.newId();
        InvitationId invitationId = InvitationId.newId();

        MembershipRequest request = MembershipRequest.submit(MembershipRequestId.newId(),
                EntityId.of(invitationId.asUuid()), propertyId, unitId, partyId, userId);
        MembershipRequest sibling = MembershipRequest.submit(MembershipRequestId.newId(),
                EntityId.of(invitationId.asUuid()), propertyId, unitId, EntityId.newId(), EntityId.newId());

        Invitation invitation = Invitation.issue(invitationId, propertyId, InvitationType.PUBLIC, "PROPERTY_OWNER",
                null, "tok", CLOCK.instant().plus(Duration.ofDays(30)), EntityId.newId(), null, null, null);

        when(membershipRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(membershipRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipRequestRepository.findAllPendingByUnitId(unitId)).thenReturn(List.of(request, sibling));

        newService().accept(new AcceptMembershipRequestCommand(request.getId(), decidedByUserId));

        verify(unitDirectoryPort).claim(unitId, partyId);
        verify(accountDirectoryPort).grantPropertyRole(userId, partyId, propertyId, "PROPERTY_OWNER");

        ArgumentCaptor<MembershipRequest> captor = ArgumentCaptor.forClass(MembershipRequest.class);
        verify(membershipRequestRepository, times(2)).save(captor.capture());
        MembershipRequest acceptedSaved = captor.getAllValues().get(0);
        MembershipRequest rejectedSibling = captor.getAllValues().get(1);
        assertThat(acceptedSaved.getId()).isEqualTo(request.getId());
        assertThat(acceptedSaved.getStatus().name()).isEqualTo("ACCEPTED");
        assertThat(rejectedSibling.getId()).isEqualTo(sibling.getId());
        assertThat(rejectedSibling.getStatus().name()).isEqualTo("REJECTED");
        assertThat(rejectedSibling.getRejectionReason()).isEqualTo("Lot attribué à un autre candidat");
    }

    /**
     * Le candidat évincé n'apprend nulle part ailleurs que le lot est parti :
     * sa demande passe de PENDING à REJECTED sans qu'il ait rien fait, et sans
     * cette annonce il continue d'attendre une validation qui ne viendra pas.
     */
    @Test
    void the_accepted_candidate_and_every_evicted_sibling_are_both_announced() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        EntityId partyId = EntityId.newId();
        EntityId decidedByUserId = EntityId.newId();
        InvitationId invitationId = InvitationId.newId();

        MembershipRequest request = MembershipRequest.submit(MembershipRequestId.newId(),
                EntityId.of(invitationId.asUuid()), propertyId, unitId, partyId, EntityId.newId());
        MembershipRequest sibling = MembershipRequest.submit(MembershipRequestId.newId(),
                EntityId.of(invitationId.asUuid()), propertyId, unitId, EntityId.newId(), EntityId.newId());
        Invitation invitation = Invitation.issue(invitationId, propertyId, InvitationType.PUBLIC, "PROPERTY_OWNER",
                null, "tok", CLOCK.instant().plus(Duration.ofDays(30)), EntityId.newId(), null, null, null);

        when(membershipRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(membershipRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipRequestRepository.findAllPendingByUnitId(unitId)).thenReturn(List.of(request, sibling));

        newService().accept(new AcceptMembershipRequestCommand(request.getId(), decidedByUserId));

        verify(eventPublisher).publishEvent(new MembershipRequestDecidedEvent(request.getId(), propertyId, unitId,
                request.getUserId(), true, decidedByUserId, null));
        verify(eventPublisher).publishEvent(new MembershipRequestDecidedEvent(sibling.getId(), propertyId, unitId,
                sibling.getUserId(), false, decidedByUserId, "Lot attribué à un autre candidat"));
    }

    @Test
    void a_request_that_cannot_be_decided_is_never_announced() {
        MembershipRequestId id = MembershipRequestId.newId();
        when(membershipRequestRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().accept(new AcceptMembershipRequestCommand(id, EntityId.newId())))
                .isInstanceOf(MembershipRequestNotFoundException.class);

        verify(eventPublisher, never()).publishEvent(any(MembershipRequestDecidedEvent.class));
    }

    /**
     * Le cas nominal d'une invitation privée : le syndic a rattaché le lot au
     * contact avant de l'inviter. Il n'y a rien à réserver, seul l'accès reste
     * à ouvrir - échouer ici bloquerait toutes les invitations privées.
     */
    @Test
    void a_lot_already_owned_by_the_requester_is_not_re_claimed_and_the_role_is_still_granted() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        EntityId partyId = EntityId.newId();
        EntityId userId = EntityId.newId();
        InvitationId invitationId = InvitationId.newId();
        MembershipRequest request = MembershipRequest.submit(MembershipRequestId.newId(),
                EntityId.of(invitationId.asUuid()), propertyId, unitId, partyId, userId);
        Invitation invitation = Invitation.issue(invitationId, propertyId, InvitationType.PRIVATE, "PROPERTY_OWNER",
                null, "tok", CLOCK.instant().plus(Duration.ofDays(30)), EntityId.newId(), null, unitId, null);

        when(membershipRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(membershipRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipRequestRepository.findAllPendingByUnitId(unitId)).thenReturn(List.of(request));
        org.mockito.Mockito.doThrow(new UnitUnavailableException("already claimed"))
                .when(unitDirectoryPort).claim(unitId, partyId);
        when(unitDirectoryPort.isOwnedBy(unitId, partyId)).thenReturn(true);

        newService().accept(new AcceptMembershipRequestCommand(request.getId(), EntityId.newId()));

        verify(accountDirectoryPort).grantPropertyRole(userId, partyId, propertyId, "PROPERTY_OWNER");
    }

    /** Un lot pris par quelqu'un d'autre, en revanche, reste un refus net. */
    @Test
    void a_lot_owned_by_someone_else_still_fails_the_acceptance() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        EntityId partyId = EntityId.newId();
        InvitationId invitationId = InvitationId.newId();
        MembershipRequest request = MembershipRequest.submit(MembershipRequestId.newId(),
                EntityId.of(invitationId.asUuid()), propertyId, unitId, partyId, EntityId.newId());
        Invitation invitation = Invitation.issue(invitationId, propertyId, InvitationType.PUBLIC, "PROPERTY_OWNER",
                null, "tok", CLOCK.instant().plus(Duration.ofDays(30)), EntityId.newId(), null, null, null);

        when(membershipRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        org.mockito.Mockito.doThrow(new UnitUnavailableException("already claimed"))
                .when(unitDirectoryPort).claim(unitId, partyId);
        when(unitDirectoryPort.isOwnedBy(unitId, partyId)).thenReturn(false);

        assertThatThrownBy(() -> newService().accept(new AcceptMembershipRequestCommand(request.getId(), EntityId.newId())))
                .isInstanceOf(UnitUnavailableException.class);

        verify(accountDirectoryPort, never()).grantPropertyRole(any(), any(), any(), any());
    }

    @Test
    void accepting_an_unknown_request_throws() {
        MembershipRequestId id = MembershipRequestId.newId();
        when(membershipRequestRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().accept(new AcceptMembershipRequestCommand(id, EntityId.newId())))
                .isInstanceOf(MembershipRequestNotFoundException.class);
    }

    @Test
    void accepting_an_already_decided_request_throws_and_does_not_claim_the_unit() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        MembershipRequest request = MembershipRequest.submit(MembershipRequestId.newId(), EntityId.newId(), propertyId,
                unitId, EntityId.newId(), EntityId.newId()).accept(CLOCK.instant(), EntityId.newId());
        when(membershipRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> newService().accept(new AcceptMembershipRequestCommand(request.getId(), EntityId.newId())))
                .isInstanceOf(MembershipRequestAlreadyDecidedException.class);

        verify(unitDirectoryPort, never()).claim(any(), any());
    }
}
