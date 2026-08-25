package com.architek.oikos.invitation.application.usecase;

import java.time.Clock;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.command.RejectMembershipRequestCommand;
import com.architek.oikos.invitation.application.event.MembershipRequestDecidedEvent;
import com.architek.oikos.invitation.application.port.in.RejectMembershipRequestUseCase;
import com.architek.oikos.invitation.domain.exception.MembershipRequestAlreadyDecidedException;
import com.architek.oikos.invitation.domain.exception.MembershipRequestNotFoundException;
import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;

/**
 * Le refus est annoncé au demandeur sur ses quatre canaux et au reste du
 * bureau une fois le refus commité (voir MembershipDecisionNotificationListener) :
 * un refus silencieux laisse le candidat attendre indéfiniment un accès qui ne
 * viendra pas.
 */
@Component
public class RejectMembershipRequestService implements RejectMembershipRequestUseCase {

    private final MembershipRequestRepository membershipRequestRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public RejectMembershipRequestService(MembershipRequestRepository membershipRequestRepository,
                                           ApplicationEventPublisher eventPublisher, Clock clock) {
        this.membershipRequestRepository = membershipRequestRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void reject(RejectMembershipRequestCommand command) {
        MembershipRequest request = membershipRequestRepository.findById(command.id())
                .orElseThrow(() -> new MembershipRequestNotFoundException(command.id()));
        if (!request.isPending()) {
            throw new MembershipRequestAlreadyDecidedException();
        }
        membershipRequestRepository.save(request.reject(clock.instant(), command.decidedByUserId(), command.reason()));
        eventPublisher.publishEvent(new MembershipRequestDecidedEvent(request.getId(), request.getPropertyId(),
                request.getUnitId(), request.getUserId(), false, command.decidedByUserId(), command.reason()));
    }
}
