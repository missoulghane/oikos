package com.architek.oikos.invitation.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.port.in.ListAvailableUnitsForInvitationUseCase;
import com.architek.oikos.invitation.application.port.out.AvailableUnitInfo;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.application.query.ListAvailableUnitsForInvitationQuery;
import com.architek.oikos.invitation.domain.exception.InvalidInvitationTokenException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.shared.domain.pagination.Page;

@Component
public class ListAvailableUnitsForInvitationService implements ListAvailableUnitsForInvitationUseCase {

    private final InvitationRepository invitationRepository;
    private final UnitDirectoryPort unitDirectoryPort;
    private final Clock clock;

    public ListAvailableUnitsForInvitationService(InvitationRepository invitationRepository, UnitDirectoryPort unitDirectoryPort,
                                                    Clock clock) {
        this.invitationRepository = invitationRepository;
        this.unitDirectoryPort = unitDirectoryPort;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AvailableUnitInfo> list(ListAvailableUnitsForInvitationQuery query) {
        Invitation invitation = invitationRepository.findByToken(query.token())
                .orElseThrow(() -> new InvalidInvitationTokenException("Invalid invitation link"));
        if (!invitation.isUsable(clock.instant())) {
            throw new InvalidInvitationTokenException("This invitation link is no longer usable");
        }
        if (invitation.getType() == InvitationType.PRIVATE_WITH_UNIT) {
            throw new InvalidInvitationTokenException("This invitation link is no longer usable");
        }
        return unitDirectoryPort.listAvailable(invitation.getPropertyId(), query.pageRequest());
    }
}
