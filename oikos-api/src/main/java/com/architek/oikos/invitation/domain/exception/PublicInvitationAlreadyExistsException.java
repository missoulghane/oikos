package com.architek.oikos.invitation.domain.exception;

import com.architek.oikos.shared.exception.ConflictException;

/**
 * Une copropriété n'a qu'un lien public, pour toujours : on le désactive et on
 * le réactive (voir EnableInvitationUseCase), on n'en crée jamais un second.
 * Un second lien invaliderait silencieusement le premier - celui dont le QR
 * code est imprimé dans le hall, collé sur les avis, partagé dans les groupes
 * de voisins - sans que personne ne s'en aperçoive avant qu'un copropriétaire
 * ne tombe sur une page morte.
 */
public class PublicInvitationAlreadyExistsException extends ConflictException {

    public PublicInvitationAlreadyExistsException() {
        super("This property already has a public invitation link: re-enable it instead of creating a new one");
    }
}
