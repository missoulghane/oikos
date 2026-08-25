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

        boolean usable = invitation.isUsable(clock.instant());
        // Le lot désigné est nommé, pas seulement identifié : « votre lot », sans
        // dire lequel, ne permet pas à l'invité de repérer une erreur du syndic.
        // Un lot supprimé depuis l'envoi laisse simplement le sélecteur reprendre
        // la main plutôt que de rendre le lien inutilisable.
        UnitBasicInfo targetUnit = invitation.getTargetUnitId() == null ? null
                : unitDirectoryPort.findBasicInfo(invitation.getTargetUnitId()).orElse(null);
        return new InvitationPreviewView(invitation.getType(), usable, usable ? null : reasonFor(invitation),
                property.name(), property.address(), invitation.getTargetEmail(), invitation.getTargetBoardRole(),
                targetUnit == null ? null : invitation.getTargetUnitId(),
                targetUnit == null ? null : targetUnit.unitNumber(),
                targetUnit == null ? null : targetUnit.unitTypeName());
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
