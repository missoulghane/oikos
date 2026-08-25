package com.architek.oikos.invitation.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.command.CreateInvitationCommand;
import com.architek.oikos.invitation.application.port.in.CreateInvitationUseCase;
import com.architek.oikos.invitation.application.port.out.PartyContactInfo;
import com.architek.oikos.invitation.application.port.out.PartyDirectoryPort;
import com.architek.oikos.invitation.application.port.out.PropertyBasicInfo;
import com.architek.oikos.invitation.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.invitation.application.port.out.UnitBasicInfo;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.domain.exception.PublicInvitationAlreadyExistsException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.service.InvitationTokenGenerator;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

/**
 * targetRole is hardcoded to PROPERTY_OWNER for now - the domain model keeps
 * it as a plain field (not a constant) so board-member invitations can reuse
 * this same use case later without a schema change, but nothing today lets a
 * caller choose a different role.
 *
 * <p>Deux formes bien distinctes. Le lien PUBLIC est unique et permanent pour
 * la copropriété, sans destinataire ni lot : il circule, chacun y choisit son
 * lot. L'invitation PRIVATE part de la fiche d'un contact, pour un lot précis,
 * et l'email part dans la foulée - « envoyer une invitation » n'est pas
 * « fabriquer un lien et se débrouiller ».
 *
 * <p>Réinviter un contact remplace l'invitation qui courait encore, au lieu
 * d'en faire courir deux : c'est la même règle que l'ancienne invitation de
 * compte (voir InvitePartyService), et sans elle « une invitation est en
 * cours » deviendrait ambigu dès le premier renvoi.
 */
@Component
public class CreateInvitationService implements CreateInvitationUseCase {

    private static final String TARGET_ROLE_OWNER = "PROPERTY_OWNER";

    private final InvitationRepository invitationRepository;
    private final PropertyDirectoryPort propertyDirectoryPort;
    private final UnitDirectoryPort unitDirectoryPort;
    private final PartyDirectoryPort partyDirectoryPort;
    private final InvitationTokenGenerator tokenGenerator;
    private final InvitationLinkComposer linkComposer;
    private final OwnerInvitationEmailComposer emailComposer;
    private final EmailSenderPort emailSenderPort;
    private final Clock clock;
    private final Duration invitationTokenTtl;

    public CreateInvitationService(InvitationRepository invitationRepository, PropertyDirectoryPort propertyDirectoryPort,
                                    UnitDirectoryPort unitDirectoryPort, PartyDirectoryPort partyDirectoryPort,
                                    InvitationTokenGenerator tokenGenerator,
                                    InvitationLinkComposer linkComposer, OwnerInvitationEmailComposer emailComposer,
                                    EmailSenderPort emailSenderPort, Clock clock,
                                    @Value("${oikos.mail.invitation-token-ttl-days}") long invitationTokenTtlDays) {
        this.invitationRepository = invitationRepository;
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.unitDirectoryPort = unitDirectoryPort;
        this.partyDirectoryPort = partyDirectoryPort;
        this.tokenGenerator = tokenGenerator;
        this.linkComposer = linkComposer;
        this.emailComposer = emailComposer;
        this.emailSenderPort = emailSenderPort;
        this.clock = clock;
        this.invitationTokenTtl = Duration.ofDays(invitationTokenTtlDays);
    }

    @Override
    @Transactional
    public InvitationId create(CreateInvitationCommand command) {
        PropertyBasicInfo property = propertyDirectoryPort.findBasicInfo(command.propertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + command.propertyId()));

        boolean isPublic = command.type() == InvitationType.PUBLIC;
        if (isPublic) {
            requirePublicShape(command);
        }
        UnitBasicInfo unit = isPublic ? null : requirePrivateUnit(command);
        PartyContactInfo contact = isPublic ? null : requirePrivateContact(command);

        if (contact != null) {
            supersedeOutstandingInvitation(command.targetPartyId());
        }

        String rawToken = tokenGenerator.generate();
        Instant expiresAt = clock.instant().plus(invitationTokenTtl);
        Invitation invitation = Invitation.issue(InvitationId.newId(), command.propertyId(), command.type(),
                TARGET_ROLE_OWNER, contact == null ? null : contact.email(), rawToken, expiresAt,
                command.createdByUserId(), null, command.unitId(), command.targetPartyId());

        InvitationId savedId = invitationRepository.save(invitation).getId();
        if (contact != null) {
            emailSenderPort.send(contact.email(), emailComposer.subject(property.name()),
                    emailComposer.htmlBody(property.name(), unitLabel(unit), linkComposer.link(rawToken)));
        }
        return savedId;
    }

    /** Une seule invitation en cours par contact : la précédente est fermée, son lien cesse de valoir. */
    private void supersedeOutstandingInvitation(EntityId partyId) {
        invitationRepository.findOutstandingPrivateByPartyId(partyId)
                .filter(outstanding -> outstanding.isUsable(clock.instant()))
                .ifPresent(outstanding -> invitationRepository.save(outstanding.disable()));
    }

    private void requirePublicShape(CreateInvitationCommand command) {
        // Un lien public ne désigne personne et ne réserve rien : lui attacher un
        // lot ou une adresse en ferait une invitation privée déguisée, avec le
        // parcours d'un lien qui circule.
        if (command.unitId() != null || command.targetPartyId() != null) {
            throw new IllegalArgumentException("A PUBLIC invitation link designates neither a unit nor a contact");
        }
        // Un seul lien public par copropriété, créé une fois puis désactivé et
        // réactivé à volonté : voir PublicInvitationAlreadyExistsException.
        if (invitationRepository.findPublicByPropertyId(command.propertyId()).isPresent()) {
            throw new PublicInvitationAlreadyExistsException();
        }
    }

    /**
     * @return le contact destinataire. Son adresse est lue ici, sur sa fiche,
     *         plutôt que reçue de l'appelant : une adresse fournie par le
     *         client pourrait ne pas être la sienne, et le lien partirait à
     *         côté.
     */
    private PartyContactInfo requirePrivateContact(CreateInvitationCommand command) {
        if (command.targetPartyId() == null) {
            throw new IllegalArgumentException("targetPartyId is required for " + command.type() + " invitations");
        }
        PartyContactInfo contact = partyDirectoryPort.findById(command.targetPartyId())
                .orElseThrow(() -> new ResourceNotFoundException("Party not found with id: " + command.targetPartyId()));
        if (!contact.propertyId().equals(command.propertyId())) {
            throw new IllegalArgumentException(
                    "Party " + command.targetPartyId() + " does not belong to property " + command.propertyId());
        }
        // Un contact qui n'a qu'un téléphone n'est pas invitable : il n'y a
        // nulle part où envoyer le lien.
        if (contact.email() == null) {
            throw new IllegalArgumentException("Party " + command.targetPartyId() + " has no email to invite");
        }
        return contact;
    }

    /** @return le lot désigné, dont le libellé part dans l'email. */
    private UnitBasicInfo requirePrivateUnit(CreateInvitationCommand command) {
        if (command.unitId() == null) {
            throw new IllegalArgumentException("unitId is required for " + command.type() + " invitations");
        }
        UnitBasicInfo unit = unitDirectoryPort.findBasicInfo(command.unitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + command.unitId()));
        // Jamais de confiance dans un identifiant venu du client : un lot d'une
        // autre copropriété ouvrirait un accès là où l'appelant n'a aucun droit.
        if (!unit.propertyId().equals(command.propertyId())) {
            throw new IllegalArgumentException("Unit " + command.unitId() + " does not belong to property " + command.propertyId());
        }
        return unit;
    }

    private static String unitLabel(UnitBasicInfo unit) {
        return unit.unitTypeName() == null ? unit.unitNumber() : unit.unitNumber() + " — " + unit.unitTypeName();
    }
}
