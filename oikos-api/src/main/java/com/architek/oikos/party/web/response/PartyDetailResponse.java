package com.architek.oikos.party.web.response;

import com.architek.oikos.party.application.dto.PartyView;
import com.architek.oikos.shared.domain.valueobject.PartyAccountStatus;
import com.architek.oikos.shared.domain.valueobject.PartyType;

/**
 * La fiche d'un contact : ses informations, plus ce que la fiche autorise -
 * identite geree par le titulaire du compte (ACTIVE), modifiable mais avec une
 * invitation en cours qui restera sur les anciennes donnees (INVITED), ou
 * librement modifiable (NONE).
 *
 * <p>Type distinct de {@link PartyResponse} plutot qu'un champ nullable de
 * plus : le statut ne decrit pas le contact mais son rapport aux comptes,
 * resolu dans le module user (GetPartyAccountStatusUseCase), et le resoudre
 * pour chaque ligne d'une liste paginee couterait une requete par ligne pour
 * une information que la liste n'affiche pas.
 */
public record PartyDetailResponse(String id, String fullName, PartyType partyType, String email, String phone,
                                    PartyAccountStatus accountStatus) {

    public static PartyDetailResponse from(PartyView view, PartyAccountStatus accountStatus) {
        return new PartyDetailResponse(view.id().toString(), view.fullName(), view.partyType(), view.email(),
                view.phone(), accountStatus);
    }
}
