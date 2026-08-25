package com.architek.oikos.invitation.application.dto;

/**
 * Statut applicatif de l'écran « Demandes d'adhésion » du syndic. Il reflète
 * désormais exactement l'énumération persistée MembershipRequestStatus.
 *
 * <p>INVITED a disparu : cet écran mélangeait les demandes d'adhésion avec les
 * invitations privées encore en attente d'acceptation, deux objets qui n'ont
 * ni les mêmes actions ni le même cycle de vie. Les invitations privées se
 * pilotent maintenant depuis la fiche du contact concerné (« Inviter à créer
 * un compte »), là où l'on sait de qui l'on parle.
 */
public enum MembershipRequestOverviewStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
