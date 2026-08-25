package com.architek.oikos.invitation.application.port.in;

import java.util.Optional;

import com.architek.oikos.invitation.application.dto.InvitationView;
import com.architek.oikos.invitation.application.query.GetPublicInvitationQuery;

/**
 * Le lien public d'une copropriété, unique et permanent (voir
 * InvitationRepository.findPublicByPropertyId). Optional.empty() tant
 * qu'aucun n'a été créé - une copropriété sans lien public est un état
 * normal, pas une erreur : c'est ce que lit l'écran « Informations
 * générales » pour proposer de le créer plutôt que de l'afficher.
 */
public interface GetPublicInvitationUseCase {

    Optional<InvitationView> getPublicInvitation(GetPublicInvitationQuery query);
}
