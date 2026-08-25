package com.architek.oikos.invitation.application.dto;

import java.time.Instant;

import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Une ligne du tableau « Demandes d'adhésion » du syndic, déjà résolue : le
 * nom et l'adresse du demandeur, le lot visé, la date de dépôt. La liste les
 * portait jusqu'ici en identifiants bruts, et le tableau allait chercher
 * chaque contact et chaque lot par une requête séparée depuis le navigateur -
 * une vingtaine d'appels pour afficher cinq lignes, et rien à quoi le serveur
 * puisse appliquer un tri ou une recherche.
 *
 * <p>requesterAccountVerified dit si le demandeur a confirmé son adresse
 * email. Une demande déposée pendant l'inscription arrive avant cette
 * confirmation (voir SubmitMembershipRequestService) : attribuer un lot à une
 * adresse jamais confirmée est une décision que le syndic doit pouvoir
 * prendre les yeux ouverts.
 */
public record MembershipRequestOverviewView(EntityId id, EntityId invitationId, EntityId propertyId, EntityId unitId,
                                             EntityId partyId, EntityId userId, String requesterFullName,
                                             String requesterEmail, boolean requesterAccountVerified, String unitNumber,
                                             String unitTypeName, MembershipRequestOverviewStatus status,
                                             Instant submittedAt, Instant decidedAt, EntityId decidedByUserId,
                                             String decidedByFullName, String rejectionReason) {

    public static MembershipRequestOverviewView of(MembershipRequest request, String requesterFullName,
                                                    String requesterEmail, boolean requesterAccountVerified,
                                                    String unitNumber, String unitTypeName, String decidedByFullName) {
        return new MembershipRequestOverviewView(EntityId.of(request.getId().asUuid()), request.getInvitationId(),
                request.getPropertyId(), request.getUnitId(), request.getPartyId(), request.getUserId(),
                requesterFullName, requesterEmail, requesterAccountVerified, unitNumber, unitTypeName,
                MembershipRequestOverviewStatus.valueOf(request.getStatus().name()), request.getSubmittedAt(),
                request.getDecidedAt(), request.getDecidedByUserId(), decidedByFullName, request.getRejectionReason());
    }
}
