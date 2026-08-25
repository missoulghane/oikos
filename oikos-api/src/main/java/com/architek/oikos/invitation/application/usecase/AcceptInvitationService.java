package com.architek.oikos.invitation.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.command.AcceptInvitationCommand;
import com.architek.oikos.invitation.application.port.in.AcceptInvitationUseCase;
import com.architek.oikos.invitation.application.port.out.AccountDirectoryPort;
import com.architek.oikos.invitation.application.port.out.AccountInfo;
import com.architek.oikos.invitation.application.port.out.BoardDirectoryPort;
import com.architek.oikos.invitation.application.port.out.PartyDetails;
import com.architek.oikos.invitation.application.port.out.PartyDirectoryPort;
import com.architek.oikos.invitation.domain.exception.InvalidInvitationTokenException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;

/**
 * Ne traite plus que les invitations au conseil syndical (targetRole
 * PROPERTY_BOARD_MEMBER) : la partie attache le contact au conseil sur un
 * siège PENDING_VALIDATION, sans lot à réserver ni ligne MembershipRequest
 * (cette trace-là suit les candidatures sur un lot). Accepter le lien ne
 * donne pas le rôle PROPERTY_BOARD_MEMBER : un administrateur doit valider le
 * siège ensuite (voir property.application.usecase.ValidateBoardMemberService).
 *
 * <p>Les invitations de copropriétaire, publiques comme privées, passent
 * désormais toutes par SubmitMembershipRequestUseCase : elles déposent une
 * demande que le syndic valide. Ce cas d'usage accordait jusqu'ici l'accès
 * sur-le-champ à une invitation privée, en réservant le lot choisi par
 * l'invité - une porte que la page d'accueil ouvre justement au changement de
 * lot (« ce n'est pas votre lot ? »), et qui n'a plus lieu d'exister sans
 * revue. Le refus ci-dessous est explicite plutôt que silencieux : un client
 * resté sur l'ancien appel doit le savoir, pas croire avoir réussi.
 */
@Component
public class AcceptInvitationService implements AcceptInvitationUseCase {

    private static final String TARGET_ROLE_BOARD_MEMBER = "PROPERTY_BOARD_MEMBER";

    private final InvitationRepository invitationRepository;
    private final PartyDirectoryPort partyDirectoryPort;
    private final AccountDirectoryPort accountDirectoryPort;
    private final BoardDirectoryPort boardDirectoryPort;
    private final Clock clock;

    public AcceptInvitationService(InvitationRepository invitationRepository, PartyDirectoryPort partyDirectoryPort,
                                    AccountDirectoryPort accountDirectoryPort, BoardDirectoryPort boardDirectoryPort,
                                    Clock clock) {
        this.invitationRepository = invitationRepository;
        this.partyDirectoryPort = partyDirectoryPort;
        this.accountDirectoryPort = accountDirectoryPort;
        this.boardDirectoryPort = boardDirectoryPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public EntityId accept(AcceptInvitationCommand command) {
        Invitation invitation = invitationRepository.findByToken(command.token())
                .orElseThrow(() -> new InvalidInvitationTokenException("Invalid invitation link"));
        if (!invitation.isUsable(clock.instant())) {
            throw new InvalidInvitationTokenException("This invitation link is no longer usable");
        }
        if (invitation.getType() != InvitationType.PRIVATE || !isBoardInvitation(invitation)) {
            throw new InvalidInvitationTokenException(
                    "This invitation opens a membership request: submit it instead of accepting it directly");
        }
        return acceptBoardInvitation(invitation, command.actingUserId());
    }

    private EntityId resolveParty(EmailVO email, String fullName, EntityId propertyId) {
        return partyDirectoryPort.findIdByEmail(email, propertyId)
                .orElseGet(() -> partyDirectoryPort.createParty(new PartyDetails(fullName, PartyType.INDIVIDUAL, email, null), propertyId));
    }

    private boolean isBoardInvitation(Invitation invitation) {
        return TARGET_ROLE_BOARD_MEMBER.equals(invitation.getTargetRole());
    }

    /**
     * No lot to claim and no MembershipRequest audit row here - that trail
     * exists to track unit-ownership candidacies, which a board seat isn't.
     * The PROPERTY_BOARD_MEMBER role is NOT granted here: the seat is
     * created PENDING_VALIDATION and only becomes ACTIVE (with the role
     * actually granted) once an admin validates it. Accepting the link only
     * consumes the invitation.
     */
    private EntityId acceptBoardInvitation(Invitation invitation, EntityId actingUserId) {
        AccountInfo accountInfo = accountDirectoryPort.getAccountInfo(actingUserId);
        EntityId partyId = resolveParty(accountInfo.email(), accountInfo.fullName(), invitation.getPropertyId());

        boardDirectoryPort.addPendingBoardMember(invitation.getPropertyId(), partyId, actingUserId, invitation.getTargetBoardRole());
        invitationRepository.save(invitation.consume(accountInfo.email()));

        return actingUserId;
    }
}
