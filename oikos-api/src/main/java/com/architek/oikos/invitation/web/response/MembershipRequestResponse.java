package com.architek.oikos.invitation.web.response;

import java.time.Instant;

import com.architek.oikos.invitation.application.dto.MembershipRequestOverviewStatus;
import com.architek.oikos.invitation.application.dto.MembershipRequestOverviewView;

/**
 * Une ligne du tableau « Demandes d'adhésion », déjà résolue côté serveur -
 * nom et email du demandeur, numéro et type de lot, nom du décideur : le
 * tableau n'a plus à rappeler l'API une fois par contact et par lot pour
 * s'afficher.
 *
 * <p>requesterFullName/requesterEmail/unitNumber peuvent être nuls si le
 * compte ou le lot a disparu depuis le dépôt : la demande reste un fait à
 * afficher, la ligne se contente de ne rien inventer.
 */
public record MembershipRequestResponse(String id, String invitationId, String propertyId, String unitId,
                                         String partyId, String userId, String requesterFullName, String requesterEmail,
                                         boolean requesterAccountVerified, String unitNumber, String unitTypeName,
                                         MembershipRequestOverviewStatus status, Instant submittedAt, Instant decidedAt,
                                         String decidedByUserId, String decidedByFullName, String rejectionReason) {

    public static MembershipRequestResponse from(MembershipRequestOverviewView view) {
        return new MembershipRequestResponse(view.id().toString(), view.invitationId().toString(),
                view.propertyId().toString(), view.unitId() != null ? view.unitId().toString() : null,
                view.partyId() != null ? view.partyId().toString() : null,
                view.userId() != null ? view.userId().toString() : null,
                view.requesterFullName(), view.requesterEmail(), view.requesterAccountVerified(), view.unitNumber(),
                view.unitTypeName(), view.status(), view.submittedAt(), view.decidedAt(),
                view.decidedByUserId() != null ? view.decidedByUserId().toString() : null, view.decidedByFullName(),
                view.rejectionReason());
    }
}
