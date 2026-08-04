package com.architek.oikos.invitation.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.dto.InvitationPreviewView;
import com.architek.oikos.invitation.application.port.in.GetInvitationByTokenUseCase;
import com.architek.oikos.invitation.application.port.out.PropertyBasicInfo;
import com.architek.oikos.invitation.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.invitation.application.port.out.UnitBasicInfo;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.application.query.GetInvitationByTokenQuery;
import com.architek.oikos.invitation.domain.exception.InvalidInvitationTokenException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationStatus;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;

@Component
public class GetInvitationByTokenService implements GetInvitationByTokenUseCase {

    private final InvitationRepository invitationRepository;
    private final PropertyDirectoryPort propertyDirectoryPort;
    private final UnitDirectoryPort unitDirectoryPort;
    private final Clock clock;

    public GetInvitationByTokenService(InvitationRepository invitationRepository, PropertyDirectoryPort propertyDirectoryPort,
                                        UnitDirectoryPort unitDirectoryPort, Clock clock) {
        this.invitationRepository = invitationRepository;
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.unitDirectoryPort = unitDirectoryPort;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public InvitationPreviewView getPreview(GetInvitationByTokenQuery query) {
        Invitation invitation = invitationRepository.findByToken(query.token())
                .orElseThrow(() -> new InvalidInvitationTokenException("Invalid invitation link"));

        PropertyBasicInfo property = propertyDirectoryPort.findBasicInfo(invitation.getPropertyId())
                .orElseThrow(() -> new InvalidInvitationTokenException("Invalid invitation link"));

        String unitNumber = null;
        String unitTypeName = null;
        if (invitation.getType() == InvitationType.PRIVATE_WITH_UNIT) {
            UnitBasicInfo unit = unitDirectoryPort.findBasicInfo(invitation.getUnitId())
                    .orElseThrow(() -> new InvalidInvitationTokenException("Invalid invitation link"));
            unitNumber = unit.unitNumber();
            unitTypeName = unit.unitTypeName();
        }

        boolean usable = invitation.isUsable(clock.instant());
        return new InvitationPreviewView(invitation.getType(), usable, usable ? null : reasonFor(invitation),
                property.name(), property.address(), unitNumber, unitTypeName, invitation.getTargetEmail());
    }

    private String reasonFor(Invitation invitation) {
        if (invitation.getStatus() == InvitationStatus.DISABLED) {
            return "DISABLED";
        }
        if (invitation.getStatus() == InvitationStatus.CONSUMED) {
            return "CONSUMED";
        }
        return "EXPIRED";
    }
}
