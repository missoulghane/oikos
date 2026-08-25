package com.architek.oikos.invitation.web.response;

import java.util.Optional;

import com.architek.oikos.invitation.application.dto.InvitationView;

/**
 * Enveloppe volontaire autour d'un lien nullable, plutôt qu'un 404 ou un 204 :
 * une copropriété sans lien public est un état normal que l'écran doit savoir
 * afficher (« créez-en un »), pas une absence de ressource. Un 404 aurait forcé
 * le client à traiter une erreur comme un cas nominal, et un 204 lui aurait
 * rendu un corps vide qu'aucun typage ne distingue d'une panne.
 */
public record PublicInvitationResponse(InvitationResponse invitation) {

    public static PublicInvitationResponse from(Optional<InvitationView> view) {
        return new PublicInvitationResponse(view.map(InvitationResponse::from).orElse(null));
    }
}
