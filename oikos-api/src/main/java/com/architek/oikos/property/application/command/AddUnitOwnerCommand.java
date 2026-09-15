package com.architek.oikos.property.application.command;

import java.math.BigDecimal;

import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;

/**
 * {@code invite} : envoyer au copropriétaire l'invitation qui le rattachera à
 * ce lot, la même que depuis sa fiche. Sans effet s'il a déjà un compte.
 *
 * <p>{@code invitedByUserId} : l'auteur de l'invitation, porté par la commande
 * parce qu'une invitation en garde trace (voir Invitation) et qu'un cas
 * d'usage ne lit pas le contexte de sécurité.
 */
public record AddUnitOwnerCommand(UnitId unitId, String fullName, PartyType partyType, EmailVO email, String phone,
                                      BigDecimal ownershipShare, boolean invite, EntityId invitedByUserId) {
}
