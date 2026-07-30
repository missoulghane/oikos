package com.architek.oikos.accounting.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.accounting.domain.model.LettrageAllocationLine;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;

public record LettrageProposalLineView(UnitAccountMovementId debitMovementId, UnitAccountMovementId creditMovementId,
                                        BigDecimal amount) {

    public static LettrageProposalLineView from(LettrageAllocationLine line) {
        return new LettrageProposalLineView(line.debitMovementId(), line.creditMovementId(), line.amount());
    }
}
