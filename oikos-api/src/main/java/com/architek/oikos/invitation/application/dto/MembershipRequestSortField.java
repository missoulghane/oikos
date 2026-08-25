package com.architek.oikos.invitation.application.dto;

/**
 * Colonnes triables du tableau « Demandes d'adhésion ». SUBMITTED_AT est le
 * défaut, en décroissant : une file d'attente se lit par le haut, la plus
 * récente d'abord.
 */
public enum MembershipRequestSortField {
    SUBMITTED_AT,
    REQUESTER,
    UNIT,
    STATUS
}
