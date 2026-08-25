package com.architek.oikos.invitation.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.architek.oikos.invitation.application.event.MembershipRequestDecidedEvent;

/**
 * Annonce la décision une fois qu'elle est durablement écrite, et jamais dans
 * sa transaction.
 *
 * <p>AFTER_COMMIT vaut pour deux raisons. Un copropriétaire ne doit pas
 * recevoir « votre lot vous est attribué » pour une décision qui peut encore
 * être annulée ; et surtout, un canal qui casse ne doit pas emporter la
 * décision avec lui. Ce second point n'est pas théorique : la messagerie et les
 * notifications sont elles-mêmes transactionnelles, et une exception levée dans
 * une transaction REQUIRED imbriquée marque la transaction partagée
 * rollback-only - la rattraper plus haut n'y change rien, le commit échoue
 * quand même. Un email refusé aurait suffi à défaire une validation de syndic.
 *
 * <p>Les écritures de l'annonce s'ouvrent chacune leur propre transaction
 * (REQUIRES_NEW sur les adaptateurs, voir InvitationNotificationAdapter) : en
 * phase after-commit, la synchronisation de la transaction d'origine est encore
 * attachée au thread, et une transaction REQUIRED s'y raccrocherait pour voir
 * ses écritures jetées en silence - le même piège que documentent
 * GeneratePaymentReceiptService et SendConvocationService.
 */
@Component
public class MembershipDecisionNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(MembershipDecisionNotificationListener.class);

    private final MembershipDecisionNotifier decisionNotifier;

    MembershipDecisionNotificationListener(MembershipDecisionNotifier decisionNotifier) {
        this.decisionNotifier = decisionNotifier;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMembershipRequestDecided(MembershipRequestDecidedEvent event) {
        try {
            decisionNotifier.announce(event);
        } catch (RuntimeException e) {
            log.error("Announcement failed for membership request {} - the decision itself stands", event.requestId(), e);
        }
    }
}
