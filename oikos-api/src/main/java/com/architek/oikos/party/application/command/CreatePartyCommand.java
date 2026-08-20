package com.architek.oikos.party.application.command;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

/**
 * {@code invite} : envoyer au contact le lien de création de compte. Les
 * appelants internes (rattachement d'un lot, invitation d'un copropriétaire)
 * passent false et décident eux-mêmes de l'invitation, une fois la fiche
 * résolue - sinon un contact retrouvé et un contact créé ne recevraient pas le
 * même courrier.
 */
public record CreatePartyCommand(EntityId propertyId, String fullName, PartyType partyType, EmailVO email, String phone,
                                     boolean invite) {
}
