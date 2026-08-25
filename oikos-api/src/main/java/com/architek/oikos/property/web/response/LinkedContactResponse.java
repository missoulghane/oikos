package com.architek.oikos.property.web.response;

import java.util.Optional;

import com.architek.oikos.property.application.dto.LinkedContactView;

/**
 * Enveloppe volontaire autour d'un contact nullable, plutôt qu'un 404 :
 * « aucun compte ne porte cette adresse » est la réponse attendue dans
 * l'immense majorité des frappes, pas une absence de ressource.
 */
public record LinkedContactResponse(String partyId, String fullName, String email) {

    public static LinkedContactResponse from(Optional<LinkedContactView> view) {
        return view.map(found -> new LinkedContactResponse(found.partyId().toString(), found.fullName(), found.email()))
                .orElse(null);
    }
}
