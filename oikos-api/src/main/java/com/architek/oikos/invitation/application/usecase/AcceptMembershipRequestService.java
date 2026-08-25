package com.architek.oikos.invitation.application.usecase;

import java.time.Clock;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.command.AcceptMembershipRequestCommand;
import com.architek.oikos.invitation.application.event.MembershipRequestDecidedEvent;
import com.architek.oikos.invitation.application.port.in.AcceptMembershipRequestUseCase;
import com.architek.oikos.invitation.application.port.out.AccountDirectoryPort;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.domain.exception.InvitationNotFoundException;
import com.architek.oikos.invitation.domain.exception.MembershipRequestAlreadyDecidedException;
import com.architek.oikos.invitation.domain.exception.MembershipRequestNotFoundException;
import com.architek.oikos.invitation.domain.exception.UnitUnavailableException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Claims the unit atomically (same pessimistic lock as
 * AcceptInvitationService, via UnitDirectoryPort), grants the role, then
 * auto-rejects every other still-PENDING request on the same unit - they can
 * no longer succeed once one candidate has been seated, and leaving them
 * PENDING would strand managers with stale review items. Sibling rows are
 * kept, never deleted, per product decision.
 *
 * <p>Chaque décision est annoncée après le commit (voir
 * MembershipDecisionNotificationListener), y compris celle des candidats
 * évincés : c'est la seule façon dont ils apprennent que le lot est parti.
 * Aucun canal d'annonce ne peut faire échouer l'attribution.
 */
@Component
public class AcceptMembershipRequestService implements AcceptMembershipRequestUseCase {

    private static final String SIBLING_REJECTION_REASON = "Lot attribué à un autre candidat";

    private final MembershipRequestRepository membershipRequestRepository;
    private final InvitationRepository invitationRepository;
    private final UnitDirectoryPort unitDirectoryPort;
    private final AccountDirectoryPort accountDirectoryPort;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public AcceptMembershipRequestService(MembershipRequestRepository membershipRequestRepository,
                                           InvitationRepository invitationRepository, UnitDirectoryPort unitDirectoryPort,
                                           AccountDirectoryPort accountDirectoryPort,
                                           ApplicationEventPublisher eventPublisher, Clock clock) {
        this.membershipRequestRepository = membershipRequestRepository;
        this.invitationRepository = invitationRepository;
        this.unitDirectoryPort = unitDirectoryPort;
        this.accountDirectoryPort = accountDirectoryPort;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void accept(AcceptMembershipRequestCommand command) {
        MembershipRequest request = membershipRequestRepository.findById(command.id())
                .orElseThrow(() -> new MembershipRequestNotFoundException(command.id()));
        if (!request.isPending()) {
            throw new MembershipRequestAlreadyDecidedException();
        }
        Invitation invitation = invitationRepository.findById(InvitationId.of(request.getInvitationId().value()))
                .orElseThrow(() -> new InvitationNotFoundException(InvitationId.of(request.getInvitationId().value())));

        claimUnlessAlreadyOwned(request.getUnitId(), request.getPartyId());
        accountDirectoryPort.grantPropertyRole(request.getUserId(), request.getPartyId(), request.getPropertyId(),
                invitation.getTargetRole());
        membershipRequestRepository.save(request.accept(clock.instant(), command.decidedByUserId()));
        eventPublisher.publishEvent(decided(request, true, command.decidedByUserId(), null));

        List<MembershipRequest> siblings = membershipRequestRepository.findAllPendingByUnitId(request.getUnitId());
        for (MembershipRequest sibling : siblings) {
            if (!sibling.getId().equals(request.getId())) {
                membershipRequestRepository.save(
                        sibling.reject(clock.instant(), command.decidedByUserId(), SIBLING_REJECTION_REASON));
                eventPublisher.publishEvent(decided(sibling, false, command.decidedByUserId(), SIBLING_REJECTION_REASON));
            }
        }
    }

    /**
     * Le cas nominal d'une invitation privée : le syndic a rattaché le lot au
     * contact avant de l'inviter, il n'y a donc rien à réserver - seul l'accès
     * reste à ouvrir. Vérifier après l'échec plutôt qu'avant garde la
     * réservation atomique : un contrôle préalable rouvrirait la fenêtre de
     * concurrence que le verrou de ClaimUnitOwnershipService ferme.
     */
    private void claimUnlessAlreadyOwned(EntityId unitId, EntityId partyId) {
        try {
            unitDirectoryPort.claim(unitId, partyId);
        } catch (UnitUnavailableException e) {
            if (!unitDirectoryPort.isOwnedBy(unitId, partyId)) {
                throw e;
            }
        }
    }

    private static MembershipRequestDecidedEvent decided(MembershipRequest request, boolean accepted,
                                                          EntityId decidedByUserId, String reason) {
        return new MembershipRequestDecidedEvent(request.getId(), request.getPropertyId(), request.getUnitId(),
                request.getUserId(), accepted, decidedByUserId, reason);
    }
}
