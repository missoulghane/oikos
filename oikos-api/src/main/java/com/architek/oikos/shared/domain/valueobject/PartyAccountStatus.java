package com.architek.oikos.shared.domain.valueobject;

/**
 * Ou en est un contact (Party) vis-a-vis d'un compte de la plateforme. Vit
 * dans shared parce que la question se pose dans party (la fiche contact) et
 * se repond dans user (comptes et invitations), comme PartyType.
 *
 * <p>ACTIVE des qu'un compte est rattache au contact, meme desactive : c'est
 * ce compte qui porte alors l'identite de la personne, et la fiche n'en est
 * plus que la copie du syndic. INVITED est un etat transitoire - une
 * invitation a ete envoyee et court toujours (voir PartyInvitationToken) mais
 * personne ne l'a encore acceptee, donc aucun compte n'existe. Une invitation
 * expiree n'est plus "en cours" : elle ne peut plus etre acceptee, et le
 * contact retombe en NONE.
 */
public enum PartyAccountStatus {
    ACTIVE,
    INVITED,
    NONE
}
