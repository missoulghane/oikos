package com.architek.oikos.accounting.application.dto;

import java.math.BigDecimal;

/** Spec &sect;16 "Tresorerie": solde caisse / solde banques (banque + mobile money) / solde total. */
public record TreasurySummaryView(BigDecimal cashBalance, BigDecimal bankBalance, BigDecimal totalBalance) {
}
