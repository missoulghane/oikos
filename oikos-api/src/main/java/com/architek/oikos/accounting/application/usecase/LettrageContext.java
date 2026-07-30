package com.architek.oikos.accounting.application.usecase;

import java.util.List;

import com.architek.oikos.accounting.domain.model.LettrageProposal;
import com.architek.oikos.accounting.domain.model.UnitAccountMovement;

/** Avoids re-fetching the movement list once it's already been loaded to compute the proposal. */
record LettrageContext(List<UnitAccountMovement> movements, LettrageProposal proposal) {
}
