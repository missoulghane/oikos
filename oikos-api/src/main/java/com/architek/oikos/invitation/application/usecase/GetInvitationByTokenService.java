package com.architek.oikos.invitation.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.dto.InvitationPreviewView;
import com.architek.oikos.invitation.application.port.in.GetInvitationByTokenUseCase;
import com.architek.oikos.invitation.application.port.out.PropertyBasicInfo;
import com.architek.oikos.invitation.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.invitation.application.query.GetInvitationByTokenQuery;
import com.architek.oikos.invitation.domain.exception.InvalidInvitationTokenException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationStatus;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;

@Component
public class GetInvitationByTokenService implements GetInvitationByTokenUseCase {

    private final InvitationRepository invitationRepository;
    private final PropertyDirectoryPort propertyDirectoryPort;
    private final Clock clock;

    public GetInvitationByTokenService(InvitationRepository invitationRepository, PropertyDirectoryPort propertyDirectoryPort,
                                        Clock clock) {
        this.invitationRepository = invitationRepository;
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public InvitationPreviewView getPreview(GetInvitationByTokenQuery query) {
        Invitation invitation = invitationRepository.findByToken(query.token())
                .orElseThrow(() -> new InvalidInvitationTokenException("Invalid invitation link"));

        PropertyBasicInfo property = propertyDirectoryPort.findBasicInfo(invitation.getPropertyId())
                .orElseThrow(() -> new InvalidInvitationTokenException("Invalid invitation link"));

        boolean usable = invitation.isUsable(clock.instant());
        return new InvitationPreviewView(invitation.getType(), usable, usable ? null : reasonFor(invitation),
                property.name(), property.address(), invitation.getTargetEmail(), invitation.getTargetBoardRole());
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
