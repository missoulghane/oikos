package com.architek.oikos.party.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.architek.oikos.shared.domain.valueobject.PartyType;

public record CreatePartyRequest(
        @NotBlank String propertyId,
        @NotBlank @Size(max = 200) String fullName,
        @NotNull PartyType partyType,
        /** Facultatif : un contact peut n'avoir qu'un téléphone (voir Party). */
        @Email @Size(max = 150) String email,
        @Size(max = 20) String phone,
        /**
         * Envoyer l'invitation à créer un compte. Absent vaut true : le seul
         * appelant est l'écran de saisie d'un contact, où la case est cochée
         * par défaut - un client qui ne connaît pas encore le champ obtient donc
         * le comportement que cet écran affiche.
         */
        Boolean invite) {

    public boolean inviteOrDefault() {
        return invite == null || invite;
    }
}
