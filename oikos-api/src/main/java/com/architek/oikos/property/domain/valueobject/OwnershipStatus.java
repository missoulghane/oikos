package com.architek.oikos.property.domain.valueobject;

/**
 * RG-LOT-01: statut d'occupation d'un unit, calcule a la lecture selon la
 * presence d'au moins un UnitOwnership associe - jamais stocke sur le unit
 * lui-meme.
 */
public enum OwnershipStatus {
    SOLD,
    UNSOLD_DEVELOPER
}
