package com.architek.oikos.invitation.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EmailVO;

/**
 * L'identité d'un compte telle qu'invitation a besoin de la lire : de quoi
 * nommer un demandeur dans la liste du syndic, et de quoi le joindre une fois
 * sa demande tranchée (email, et téléphone pour WhatsApp - nul si le compte
 * n'en a pas déclaré). {@code verified} dit si l'adresse email a été
 * confirmée : une demande déposée pendant l'inscription arrive avant cette
 * confirmation, et le syndic gagne à le voir avant d'attribuer un lot.
 */
public record AccountInfo(EmailVO email, String fullName, String phone, boolean verified) {
}
