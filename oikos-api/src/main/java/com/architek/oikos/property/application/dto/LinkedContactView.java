package com.architek.oikos.property.application.dto;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * La fiche contact, dans une copropriété, du titulaire d'une adresse de
 * connexion donnée. {@code email} est celle de la fiche - souvent différente
 * de celle qu'on a cherchée, et c'est tout l'intérêt de la renvoyer : l'écran
 * peut dire « vous avez tapé son adresse de connexion, voici son contact ».
 */
public record LinkedContactView(EntityId partyId, String fullName, String email) {
}
