package com.architek.oikos.invitation.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Optional;

import com.architek.oikos.invitation.application.command.SubmitMembershipRequestCommand;
import com.architek.oikos.invitation.application.port.in.SubmitMembershipRequestUseCase;
import com.architek.oikos.invitation.application.port.out.AccountDirectoryPort;
import com.architek.oikos.invitation.application.port.out.AccountInfo;
import com.architek.oikos.invitation.application.port.out.BoardStaffDirectoryPort;
import com.architek.oikos.invitation.application.port.out.NotificationPort;
import com.architek.oikos.invitation.application.port.out.PartyDetails;
import com.architek.oikos.invitation.application.port.out.PartyDirectoryPort;
import com.architek.oikos.invitation.application.port.out.UnitBasicInfo;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.domain.exception.InvalidInvitationTokenException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;

/**
 * Le point d'entrée unique des deux invitations de copropriétaire, publique
 * et privée : toutes deux déposent une demande que le syndic valide. Une
 * invitation privée ne donne plus l'accès sur-le-champ, et c'est ce qui rend
 * sûr le « ce n'est pas votre lot ? » de la page d'accueil - sans revue,
 * l'invité s'attribuerait tout seul un lot que le syndic ne lui destinait pas.
 * (Les invitations au conseil syndical, elles, restent sur
 * AcceptInvitationService : un siège n'est pas un lot.)
 *
 * <p>Deux différences entre les deux formes, et deux seulement. Le lot : un
 * lien public n'en désigne aucun, une invitation privée en propose un que
 * l'invité peut remplacer. Le jeton : un lien public est réutilisable et
 * survit à la demande, une invitation privée est à usage unique et se
 * consomme ici même.
 *
 * <p>No unit lock is taken here: per product decision, a unit stays selectable
 * by other candidates while requests are pending - only
 * AcceptMembershipRequestService claims it, atomically, at decision time.
 */
@Component
public class SubmitMembershipRequestService implements SubmitMembershipRequestUseCase {

    private static final String TARGET_ROLE_BOARD_MEMBER = "PROPERTY_BOARD_MEMBER";

    private final InvitationRepository invitationRepository;
    private final MembershipRequestRepository membershipRequestRepository;
    private final PartyDirectoryPort partyDirectoryPort;
    private final UnitDirectoryPort unitDirectoryPort;
    private final AccountDirectoryPort accountDirectoryPort;
    private final BoardStaffDirectoryPort boardStaffDirectoryPort;
    private final NotificationPort notificationPort;
    private final Clock clock;

    public SubmitMembershipRequestService(InvitationRepository invitationRepository,
                                           MembershipRequestRepository membershipRequestRepository,
                                           PartyDirectoryPort partyDirectoryPort, UnitDirectoryPort unitDirectoryPort,
                                           AccountDirectoryPort accountDirectoryPort,
                                           BoardStaffDirectoryPort boardStaffDirectoryPort,
                                           NotificationPort notificationPort, Clock clock) {
        this.invitationRepository = invitationRepository;
        this.membershipRequestRepository = membershipRequestRepository;
        this.partyDirectoryPort = partyDirectoryPort;
        this.unitDirectoryPort = unitDirectoryPort;
        this.accountDirectoryPort = accountDirectoryPort;
        this.boardStaffDirectoryPort = boardStaffDirectoryPort;
        this.notificationPort = notificationPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public MembershipRequestId submit(SubmitMembershipRequestCommand command) {
        Invitation invitation = invitationRepository.findByToken(command.token())
                .orElseThrow(() -> new InvalidInvitationTokenException("Invalid invitation link"));
        if (isBoardInvitation(invitation)) {
            throw new InvalidInvitationTokenException("This invitation link is no longer usable");
        }
        // Le lot désigné par l'invitation privée sert de défaut ; l'invité peut le
        // remplacer depuis la page d'accueil (« ce n'est pas votre lot ? »), et
        // c'est le syndic qui tranchera.
        EntityId unitId = command.unitId() != null ? command.unitId() : invitation.getTargetUnitId();
        if (unitId == null) {
            throw new IllegalArgumentException("unitId is required to submit a membership request");
        }

        // Idempotence avant tout contrôle d'usabilité : un lien privé est à usage
        // unique et se consomme plus bas, donc un second appel du même compte sur
        // le même lot (rechargement, dépôt à l'inscription suivi de la reprise
        // après connexion, double-clic) retombe forcément sur un lien CONSUMED.
        // C'est sa propre demande, pas un lien mort.
        EntityId invitationId = EntityId.of(invitation.getId().asUuid());
        Optional<MembershipRequest> existing =
                membershipRequestRepository.findByInvitationIdAndUnitIdAndUserId(invitationId, unitId, command.actingUserId());
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        if (!invitation.isUsable(clock.instant())) {
            throw new InvalidInvitationTokenException("This invitation link is no longer usable");
        }
        UnitBasicInfo unit = unitDirectoryPort.findBasicInfo(unitId)
                .orElseThrow(() -> new IllegalArgumentException("Unit not found with id: " + unitId));
        if (!unit.propertyId().equals(invitation.getPropertyId())) {
            throw new IllegalArgumentException("Unit " + unitId + " does not belong to this invitation's property");
        }

        AccountInfo accountInfo = accountDirectoryPort.getAccountInfo(command.actingUserId());
        EntityId userId = command.actingUserId();
        EntityId partyId = resolveParty(invitation, accountInfo);

        MembershipRequest request = MembershipRequest.submit(MembershipRequestId.newId(), invitationId,
                invitation.getPropertyId(), unitId, partyId, userId);
        MembershipRequestId savedId = membershipRequestRepository.save(request).getId();

        // Un lien privé est nominatif et à usage unique : le laisser ACTIVE
        // permettrait à qui l'a reçu par ricochet - une capture d'écran, un mail
        // transféré - de déposer une seconde demande sur la même copropriété. Le
        // lien public, lui, est fait pour resservir et n'est jamais consommé.
        if (invitation.getType() == InvitationType.PRIVATE) {
            invitationRepository.save(invitation.consume(accountInfo.email()));
        }

        notifyStaff(invitation.getPropertyId(), savedId, accountInfo.fullName(), unit.unitNumber());
        return savedId;
    }

    private static boolean isBoardInvitation(Invitation invitation) {
        return TARGET_ROLE_BOARD_MEMBER.equals(invitation.getTargetRole());
    }

    /**
     * Une invitation privée est adressée à un contact que le syndic connaît
     * déjà, et à qui le lot est souvent déjà rattaché : elle porte son
     * identifiant, et c'est lui qu'on reprend - même si l'invité crée son
     * compte avec une autre adresse, et même si le syndic a corrigé la fiche
     * entre-temps. Sans cela on fabriquerait un second contact pour la même
     * personne, et l'attribution du lot échouerait à la validation - il
     * appartient déjà au premier.
     *
     * <p>Un lien public ne désigne personne : seul le compte parle.
     */
    private EntityId resolveParty(Invitation invitation, AccountInfo accountInfo) {
        if (invitation.getTargetPartyId() != null) {
            return invitation.getTargetPartyId();
        }
        EntityId propertyId = invitation.getPropertyId();
        return partyDirectoryPort.findIdByEmail(accountInfo.email(), propertyId)
                .orElseGet(() -> partyDirectoryPort.createParty(
                        new PartyDetails(accountInfo.fullName(), PartyType.INDIVIDUAL, accountInfo.email(), null), propertyId));
    }

    /**
     * REQUEST_RECEIVED (GAP.md §3.1): only fired on an actual new request, never on the idempotent-duplicate early return above.
     *
     * <p>Prévenu au dépôt, et non à la vérification du compte du demandeur.
     * Une demande déposée pendant l'inscription (voir RegisterUserService)
     * arrive donc avant que l'adresse email ne soit confirmée : c'est assumé -
     * le syndic voit sa file se remplir en temps réel, et l'état du compte lui
     * est affiché dans la liste (voir MembershipRequestOverviewView.requesterAccountVerified)
     * pour qu'il attribue un lot en connaissance de cause plutôt que sur la
     * foi d'une adresse jamais confirmée.
     *
     * <p>Le lien pointe la ligne exacte dans le tableau des demandes, espace
     * syndic imposé (voir MembershipRequestLinkComposer).
     */
    private void notifyStaff(EntityId propertyId, MembershipRequestId requestId, String requesterFullName, String unitNumber) {
        String title = "Nouvelle demande d'adhésion";
        String body = requesterFullName + " souhaite rejoindre le lot " + unitNumber + ".";
        String linkPath = MembershipRequestLinkComposer.boardRequestPath(propertyId, requestId);
        for (EntityId staffUserId : boardStaffDirectoryPort.listStaffUserIds(propertyId)) {
            notificationPort.notifyRequestReceived(staffUserId, propertyId, title, body, linkPath);
        }
    }
}
