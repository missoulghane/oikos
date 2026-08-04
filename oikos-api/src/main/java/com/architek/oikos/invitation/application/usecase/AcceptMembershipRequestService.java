package com.architek.oikos.invitation.application.usecase;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.command.AcceptMembershipRequestCommand;
import com.architek.oikos.invitation.application.port.in.AcceptMembershipRequestUseCase;
import com.architek.oikos.invitation.application.port.out.AccountDirectoryPort;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.domain.exception.InvitationNotFoundException;
import com.architek.oikos.invitation.domain.exception.MembershipRequestAlreadyDecidedException;
import com.architek.oikos.invitation.domain.exception.MembershipRequestNotFoundException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;

/**
 * Claims the unit atomically (same pessimistic lock as
 * AcceptInvitationService, via UnitDirectoryPort), grants the role, then
 * auto-rejects every other still-PENDING request on the same unit - they can
 * no longer succeed once one candidate has been seated, and leaving them
 * PENDING would strand managers with stale review items. Sibling rows are
 * kept, never deleted, per product decision.
 */
@Component
public class AcceptMembershipRequestService implements AcceptMembershipRequestUseCase {

    private static final String SIBLING_REJECTION_REASON = "Lot attribué à un autre candidat";

    private final MembershipRequestRepository membershipRequestRepository;
    private final InvitationRepository invitationRepository;
    private final UnitDirectoryPort unitDirectoryPort;
    private final AccountDirectoryPort accountDirectoryPort;
    private final Clock clock;

    public AcceptMembershipRequestService(MembershipRequestRepository membershipRequestRepository,
                                           InvitationRepository invitationRepository, UnitDirectoryPort unitDirectoryPort,
                                           AccountDirectoryPort accountDirectoryPort, Clock clock) {
        this.membershipRequestRepository = membershipRequestRepository;
        this.invitationRepository = invitationRepository;
        this.unitDirectoryPort = unitDirectoryPort;
        this.accountDirectoryPort = accountDirectoryPort;
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

        unitDirectoryPort.claim(request.getUnitId(), request.getPartyId());
        accountDirectoryPort.grantPropertyRole(request.getUserId(), request.getPartyId(), request.getPropertyId(),
                invitation.getTargetRole());
        membershipRequestRepository.save(request.accept(clock.instant(), command.decidedByUserId()));

        List<MembershipRequest> siblings = membershipRequestRepository.findAllPendingByUnitId(request.getUnitId());
        for (MembershipRequest sibling : siblings) {
            if (!sibling.getId().equals(request.getId())) {
                membershipRequestRepository.save(
                        sibling.reject(clock.instant(), command.decidedByUserId(), SIBLING_REJECTION_REASON));
            }
        }
    }
}
