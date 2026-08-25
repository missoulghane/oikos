package com.architek.oikos.invitation.application.usecase;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;

/**
 * Chemins in-app portés par les notifications de ce module (voir
 * Notification.linkPath : un simple indice d'affichage, jamais validé contre
 * la table de routage du front).
 *
 * <p>Le suffixe {@code ?space=board&propertyId=…} n'est pas décoratif : les
 * écrans syndic et copropriétaire partagent le même compte, et une URL sans
 * espace explicite renvoie le lecteur dans son espace par défaut - un membre
 * du bureau qui est aussi copropriétaire atterrissait côté copropriétaire, sur
 * un écran où la demande n'existe pas. {@code requestId} désigne la ligne à
 * mettre en évidence dans le tableau : plutôt qu'une page de détail pour un
 * objet qui tient en cinq champs et dont les deux seules actions (valider,
 * refuser) sont déjà dans la liste, la notification ouvre la liste sur la
 * bonne ligne.
 */
final class MembershipRequestLinkComposer {

    private MembershipRequestLinkComposer() {
    }

    static String boardListPath(EntityId propertyId) {
        return "/property-mngt/properties/" + propertyId.value() + "/property/membership-requests"
                + "?space=board&propertyId=" + propertyId.value();
    }

    static String boardRequestPath(EntityId propertyId, MembershipRequestId requestId) {
        return boardListPath(propertyId) + "&requestId=" + requestId.asUuid();
    }

    /** Le demandeur n'a pas d'écran de demandes : son lot validé (ou son refus) se lit sur son tableau de bord. */
    static String requesterDashboardPath() {
        return "/dashboard?space=owner";
    }
}
