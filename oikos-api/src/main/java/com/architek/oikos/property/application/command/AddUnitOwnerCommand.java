package com.architek.oikos.property.application.command;

import java.math.BigDecimal;

import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.PartyType;

/**
 * {@code invite} : envoyer au copropriétaire le lien qui rattachera un compte à
 * sa fiche. Sans effet s'il en a déjà un.
 */
public record AddUnitOwnerCommand(UnitId unitId, String fullName, PartyType partyType, EmailVO email, String phone,
                                      BigDecimal ownershipShare, boolean invite) {
}
