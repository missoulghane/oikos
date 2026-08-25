package com.architek.oikos.invitation.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.architek.oikos.invitation.application.port.out.AccountDirectoryPort;
import com.architek.oikos.invitation.application.port.out.AccountInfo;
import com.architek.oikos.invitation.application.port.out.BoardStaffDirectoryPort;
import com.architek.oikos.invitation.application.port.out.MessagingPort;
import com.architek.oikos.invitation.application.port.out.NotificationPort;
import com.architek.oikos.invitation.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.application.event.MembershipRequestDecidedEvent;
import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.application.port.out.WhatsAppSenderPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PhoneNumberVO;

/**
 * Annonce l'issue d'une demande d'adhésion, sur les quatre canaux décidés
 * pour le demandeur (notification in-app, « Ma messagerie », email, WhatsApp)
 * et sur un seul pour le bureau (notification in-app), afin qu'une demande
 * déjà tranchée ne soit pas réexaminée par un second membre.
 *
 * <p>Un seul point d'entrée pour les deux issues, appelé aussi bien par
 * AcceptMembershipRequestService que par RejectMembershipRequestService - y
 * compris pour les demandes concurrentes automatiquement rejetées quand un
 * candidat est retenu sur le lot : leurs auteurs sont prévenus exactement
 * comme les autres, c'est même la seule façon dont ils apprennent que le lot
 * est parti.
 *
 * <p><b>Aucun canal ne peut faire échouer la décision.</b> Appelé après le
 * commit (voir MembershipDecisionNotificationListener), et chaque envoi isolé :
 * un SMTP en panne, un numéro WhatsApp mal formé ou une messagerie qui refuse
 * un destinataire non-membre sont journalisés et enjambés. Une décision de
 * syndic est un fait métier déjà écrit en base ; la rejouer parce qu'un email
 * n'est pas parti la rendrait, au mieux, deux fois.
 */
@Component
class MembershipDecisionNotifier {

    private static final Logger log = LoggerFactory.getLogger(MembershipDecisionNotifier.class);

    private final AccountDirectoryPort accountDirectoryPort;
    private final BoardStaffDirectoryPort boardStaffDirectoryPort;
    private final PropertyDirectoryPort propertyDirectoryPort;
    private final UnitDirectoryPort unitDirectoryPort;
    private final NotificationPort notificationPort;
    private final MessagingPort messagingPort;
    private final EmailSenderPort emailSenderPort;
    private final WhatsAppSenderPort whatsAppSenderPort;
    private final MembershipDecisionEmailComposer emailComposer;

    MembershipDecisionNotifier(AccountDirectoryPort accountDirectoryPort, BoardStaffDirectoryPort boardStaffDirectoryPort,
                                PropertyDirectoryPort propertyDirectoryPort, UnitDirectoryPort unitDirectoryPort,
                                NotificationPort notificationPort, MessagingPort messagingPort,
                                EmailSenderPort emailSenderPort, WhatsAppSenderPort whatsAppSenderPort,
                                MembershipDecisionEmailComposer emailComposer) {
        this.accountDirectoryPort = accountDirectoryPort;
        this.boardStaffDirectoryPort = boardStaffDirectoryPort;
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.unitDirectoryPort = unitDirectoryPort;
        this.notificationPort = notificationPort;
        this.messagingPort = messagingPort;
        this.emailSenderPort = emailSenderPort;
        this.whatsAppSenderPort = whatsAppSenderPort;
        this.emailComposer = emailComposer;
    }

    void announce(MembershipRequestDecidedEvent event) {
        String propertyName = propertyDirectoryPort.findBasicInfo(event.propertyId())
                .map(info -> info.name())
                .orElse("votre copropriété");
        String unitNumber = unitDirectoryPort.findBasicInfo(event.unitId())
                .map(info -> info.unitNumber())
                .orElse("votre lot");

        AccountInfo requester = accountDirectoryPort.getAccountInfo(event.requesterUserId());
        notifyRequester(event, propertyName, unitNumber, requester);
        notifyBoard(event, unitNumber, requester);
    }

    private void notifyRequester(MembershipRequestDecidedEvent event, String propertyName, String unitNumber,
                                  AccountInfo requester) {
        boolean accepted = event.accepted();
        String reason = event.reason();
        String title = accepted ? "Votre demande d'adhésion est validée" : "Votre demande d'adhésion a été refusée";
        String body = accepted
                ? "Le lot " + unitNumber + " de " + propertyName
                        + " vous est attribué. Sa gestion est entièrement à vous ! 🎉"
                : "Votre demande pour le lot " + unitNumber + " de " + propertyName + " n'a pas été retenue."
                        + (reason == null || reason.isBlank() ? "" : " Motif : " + reason);

        run("notification", () -> notificationPort.notifyRequestDecided(event.requesterUserId(), event.propertyId(),
                title, body, MembershipRequestLinkComposer.requesterDashboardPath()));

        // Le fil part du membre du bureau qui a tranché, pas d'un expéditeur
        // système : le demandeur peut répondre à quelqu'un.
        run("messagerie", () -> { messagingPort.sendDecisionMessage(event.propertyId(), event.decidedByUserId(),
                event.requesterUserId(), title, body); });


        run("email", () -> {
            if (accepted) {
                emailSenderPort.send(requester.email(), emailComposer.acceptedSubject(propertyName),
                        emailComposer.acceptedHtmlBody(requester.fullName(), propertyName, unitNumber));
            } else {
                emailSenderPort.send(requester.email(), emailComposer.rejectedSubject(propertyName),
                        emailComposer.rejectedHtmlBody(requester.fullName(), propertyName, unitNumber, reason));
            }
        });

        run("whatsapp", () -> {
            // Un compte sans téléphone n'est pas une anomalie : le champ est
            // facultatif à l'inscription, ce canal-là saute, les autres sont partis.
            if (requester.phone() == null || requester.phone().isBlank()) {
                return;
            }
            whatsAppSenderPort.sendText(PhoneNumberVO.of(requester.phone()), title + "\n\n" + body);
        });
    }

    /**
     * Le décideur ne se prévient pas lui-même : il vient de cliquer, il sait.
     * Le demandeur non plus, même s'il siège au bureau - il a déjà reçu les
     * quatre canaux ci-dessus, et deux notifications pour un fait donneraient
     * l'impression de deux faits.
     */
    private void notifyBoard(MembershipRequestDecidedEvent event, String unitNumber, AccountInfo requester) {
        String title = "Demande d'adhésion traitée";
        String body = requester.fullName() + " — lot " + unitNumber + " : demande "
                + (event.accepted() ? "validée" : "refusée") + " par "
                + accountDirectoryPort.getAccountInfo(event.decidedByUserId()).fullName() + ".";
        String linkPath = MembershipRequestLinkComposer.boardRequestPath(event.propertyId(), event.requestId());

        for (EntityId staffUserId : boardStaffDirectoryPort.listStaffUserIds(event.propertyId())) {
            if (staffUserId.equals(event.decidedByUserId()) || staffUserId.equals(event.requesterUserId())) {
                continue;
            }
            run("notification bureau", () -> notificationPort.notifyRequestDecided(staffUserId, event.propertyId(),
                    title, body, linkPath));
        }
    }

    private void run(String channel, Runnable delivery) {
        try {
            delivery.run();
        } catch (RuntimeException e) {
            log.warn("Membership decision announcement failed on channel {}: {}", channel, e.getMessage(), e);
        }
    }
}
