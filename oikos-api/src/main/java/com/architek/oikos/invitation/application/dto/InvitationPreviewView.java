package com.architek.oikos.invitation.application.dto;

import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Public, pre-authentication view of an invitation: usable/reason let the
 * landing page render "this link has expired/been disabled" cleanly instead
 * of a hard error.
 *
 * <p>targetUnitId/targetUnitNumber/targetUnitTypeName ne sont renseignés que
 * pour une invitation privée, qui désigne un lot (voir Invitation) : la page
 * d'accueil l'annonce au lieu d'ouvrir un sélecteur, et le nomme - « votre
 * lot », sans dire lequel, ne se vérifie pas. Nuls pour un lien public, où le
 * sélecteur reste la première étape.
 */
public record InvitationPreviewView(InvitationType type, boolean usable, String reason, String propertyName,
                                     String propertyAddress, EmailVO targetEmail, String boardRole,
                                     EntityId targetUnitId, String targetUnitNumber, String targetUnitTypeName) {
}
